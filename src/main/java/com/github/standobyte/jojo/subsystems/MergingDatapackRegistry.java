package com.github.standobyte.jojo.subsystems;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import com.github.standobyte.jojo.util.objects_java.ObjectWrapper;
import com.github.standobyte.jojo.util.reflection.ReflectionUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.RegistryBuilder;

// an overengineered vanilla object vs a dumbass modder force
public class MergingDatapackRegistry {
	public static final Map<ResourceLocation, BinaryOperator<?>> MERGE_ELEMENTS = new HashMap<>();
	
	private static final Field RegistryDataLoader_LOGGER = ObfuscationReflectionHelper.findField(RegistryDataLoader.class, "LOGGER");
	private static final Field RegistryDataLoader_REGISTRATION_INFO_CACHE = ObfuscationReflectionHelper.findField(RegistryDataLoader.class, "REGISTRATION_INFO_CACHE");
	private static final Field NEO_RegistryBuilder_registryKey = ObfuscationReflectionHelper.findField(RegistryBuilder.class, "registryKey");
	
	public static <E> void makeMerging(RegistryBuilder<E> registryBuilder, BinaryOperator<E> merger) {
		ResourceKey<E> registryKey = ReflectionUtil.getFieldValue(NEO_RegistryBuilder_registryKey, registryBuilder);
		ResourceLocation id = registryKey.location();
		MERGE_ELEMENTS.put(id, merger);
	}
	
	@ApiStatus.Internal
	public static <E> boolean onLoadFromResources(ResourceManager resourceManager, RegistryOps.RegistryInfoLookup registryInfoLookup,
			RegistryDataLoader.RegistryData<E> data, WritableRegistry<E> registry, Map<ResourceKey<?>, Exception> loadingErrors) {
		ResourceKey<?> registryKey = data.key();
		ResourceLocation id = registryKey.location();
		BinaryOperator<?> merge = MERGE_ELEMENTS.get(id);
		if (merge != null) {
			loadContentsFromManager(resourceManager, registryInfoLookup, registry, data.elementCodec(), (BinaryOperator<E>) merge, loadingErrors);
			return true;
		}
		
		return false;
	}

	static final Logger LOGGER = ReflectionUtil.getFieldValue(RegistryDataLoader_LOGGER, null);
	static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = ReflectionUtil.getFieldValue(RegistryDataLoader_REGISTRATION_INFO_CACHE, null);
	static <E> void loadContentsFromManager(
			ResourceManager resourceManager,
			RegistryOps.RegistryInfoLookup registryInfoLookup,
			WritableRegistry<E> registry,
			Decoder<E> codec,
			BinaryOperator<E> merger,
			Map<ResourceKey<?>, Exception> loadingErrors
			) {
		String dirPath = Registries.elementsDirPath(registry.key());
		FileToIdConverter fileToId = FileToIdConverter.json(dirPath);
		RegistryOps<JsonElement> registryOps = new net.neoforged.neoforge.common.conditions.ConditionalOps<>(RegistryOps.create(JsonOps.INSTANCE, registryInfoLookup), net.neoforged.neoforge.common.conditions.ICondition.IContext.TAGS_INVALID);

		for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToId.listMatchingResourceStacks(resourceManager).entrySet()) {
			ResourceLocation resourceLocation = entry.getKey();
			ResourceKey<E> resourceKey = ResourceKey.create(registry.key(), fileToId.fileToId(resourceLocation));
			List<Resource> resources = entry.getValue();

			try {
				ObjectWrapper<E> finalElement = new ObjectWrapper<>(null);
				RegistrationInfo firstResourceRegInfo = REGISTRATION_INFO_CACHE.apply(resources.iterator().next().knownPackInfo());
				for (Resource resource : resources) {
					Decoder<Optional<E>> decoder = net.neoforged.neoforge.common.conditions.ConditionalOps.createConditionalCodec(net.neoforged.neoforge.common.util.NeoForgeExtraCodecs.decodeOnly(codec));
					try (Reader reader = resource.openAsReader()) {
						JsonElement jsonelement = JsonParser.parseReader(reader);
						DataResult<Optional<E>> dataresult = decoder.parse(registryOps, jsonelement);
						Optional<E> candidate = dataresult.getOrThrow();
						candidate.ifPresentOrElse(e -> {
							if (finalElement.get() == null) {
								finalElement.set(e);
							}
							else {
								finalElement.set(merger.apply(finalElement.get(), e));
							}
						}, () -> {
							LOGGER.debug("Skipping loading registry entry {} as its conditions were not met", resourceKey);
						});
					}
					catch (Exception exception) {
						loadingErrors.put(resourceKey, new IllegalStateException(
								String.format(Locale.ROOT, "Failed to parse %s from pack %s", resourceLocation, resource.sourcePackId()), exception));
					}
				}
				registry.register(resourceKey, finalElement.get(), firstResourceRegInfo);
			}
			catch (Exception exception) {
				loadingErrors.put(resourceKey, new IllegalStateException(
						String.format(Locale.ROOT, "Failed to parse %s", resourceLocation), exception));
			}
		}
	}
}
