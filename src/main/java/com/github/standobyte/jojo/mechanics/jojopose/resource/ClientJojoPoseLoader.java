package com.github.standobyte.jojo.mechanics.jojopose.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPoseAnimSet.JojoPoseAnimSetData;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class ClientJojoPoseLoader extends SimplePreparableReloadListener<ClientJojoPoseLoader.Prep> {
	private static ClientJojoPoseLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new ClientJojoPoseLoader();
		}
//		event.addListener(JojoMod.resLoc("jojopose"), instance);
		event.registerReloadListener(instance);
	}
	
	public static ClientJojoPoseLoader getInstance() {
		return instance;
	}
	
	
	public Map<ResourceLocation, JojoPoseAnimSet> anims = new HashMap<>();
	
	@Nullable
	public JojoPoseAnimSet getAnimSet(ResourceLocation geckoAnimFilePath) {
		return anims.get(geckoAnimFilePath);
	}
	
	// TODO cache
	public Stream<Map.Entry<ResourceLocation, JojoPoseAnimSet>> getForCharacter(Holder<StoryCharacter> playerCharacter, @Nullable Holder<StoryPart> playerStoryPart) {
		if (playerCharacter == null) {
			return Stream.empty();
		}
		
		ResourceLocation storyPartId = playerStoryPart != null ? playerStoryPart.unwrapKey().map(ResourceKey::location).orElse(null) : null;
		return anims.entrySet().stream().filter(animSetEntry -> {
			JojoPoseAnimSet animSet = animSetEntry.getValue();
			JojoPoseAnimSetData data = animSet.data();
			return // isSameCharacter && (poseForAnyPart || playerStoryPart != null && poseFitsThisPart)
					data.character().filter(c -> playerCharacter.is(c)).isPresent()
					&& (data.storyPart().isEmpty()
							|| playerStoryPart != null 
							&& data.storyPart().map(parts -> parts.contains(storyPartId)).isPresent());
		});
	}
	

	private static final String TOP_DIR = "jojo_pose";
	private static final String ANIM_FILE_NAME = "animation.json";
	private static final String DATA_FILE_NAME = "data.json";
	
	@Override
	protected Prep prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, AnimationSet.Builder> anims = new HashMap<>();
		Map<ResourceLocation, JojoPoseAnimSetData> posesData = new HashMap<>();

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_jojo_poses")) {
			Map<ResourceLocation, List<Resource>> animResources = resourceManager.listResourceStacks(TOP_DIR, path -> path.getPath().endsWith("/" + ANIM_FILE_NAME));
			for (var resourceEntry : animResources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				ResourceLocation animPath = resourcePathFull.withPath(path -> path.substring(
						TOP_DIR.length() + 1, path.length() - (ANIM_FILE_NAME.length() + 1)));
				AnimationSet.Builder anim = AnimationLoader.loadAnimations(resourceEntry.getValue(), resourcePathFull);
				if (!anim.isEmpty()) {
					anims.put(animPath, anim);
				}
			}

			Map<ResourceLocation, Resource> dataResources = resourceManager.listResources(TOP_DIR, path -> path.getPath().endsWith("/" + DATA_FILE_NAME));
			for (var resourceEntry : dataResources.entrySet()) {
				ResourceLocation resourcePathFull = resourceEntry.getKey();
				ResourceLocation animPath = resourcePathFull.withPath(path -> path.substring(
						TOP_DIR.length() + 1, path.length() - (DATA_FILE_NAME.length() + 1)));
				try (var reader = resourceEntry.getValue().openAsReader()) {
					JsonObject json = JSONUtil.parse(reader);
					Optional<JojoPoseAnimSetData> poseData = JojoPoseAnimSetData.CODEC.decode(JsonOps.INSTANCE, json).result().map(Pair::getFirst);
					poseData.ifPresent(data -> posesData.put(animPath, data));
				}
				catch (Exception e) {
					JojoMod.getLogger().error("Failed to read JoJo pose data from {}", resourcePathFull, e);
				}
			}
		}
		
		return new Prep(anims, posesData);
	}
	
	static record Prep(
			Map<ResourceLocation, AnimationSet.Builder> anims,
			Map<ResourceLocation, JojoPoseAnimSetData> poseData) {}
	
	
	@Override
	protected void apply(Prep prep, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.anims.clear();
		prep.anims.forEach((key, animBuilder) -> {
			this.anims.put(key, new JojoPoseAnimSet(
					animBuilder.build(), 
					Optional.ofNullable(prep.poseData.get(key)).orElse(JojoPoseAnimSet.EMPTY_DATA)));
		});
		JojoMod.getLogger().info("Loaded {} JoJo pose files", this.anims.size());
	}

}
