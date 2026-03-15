package com.github.standobyte.jojo.client.entityrender.parsemodel.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.ModelFormat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Zone;
import com.github.standobyte.v1_21_4_stuff.missingmethods._ProfilerFiller;
import com.google.gson.JsonElement;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class RotpGeckoModelLoader extends SimplePreparableReloadListener<Map<ResourceLocation, LayerDefinition>> {
	private static RotpGeckoModelLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new RotpGeckoModelLoader();
		}
//		event.addListener(JojoMod.resLoc("ripples_models"), instance);
		event.registerReloadListener(instance);
	}
	
	public static RotpGeckoModelLoader getInstance() {
		return instance;
	}
	
	protected List<Consumer<RotpGeckoModelLoader>> listeners = new ArrayList<>();
	public void addListener(Consumer<RotpGeckoModelLoader> listener) {
		this.listeners.add(listener);
	}
	
	
	public Map<ResourceLocation, ResourceModelEntry> models = new HashMap<>();
	
	/**
	 * Use this to reference a model loaded from resource packs (in either Gecko or Blockbench generic format).
	 * The model will get updated on resource reload (F3+T) inside the ResourceModelEntry object, without the need of updating the reference manually.
	 */
	@Nonnull
	public ResourceModelEntry getModelContainer(ResourceLocation path) {
		return models.computeIfAbsent(path, ResourceModelEntry::new);
	}
	
	@Nullable
	public LayerDefinition getModelDefinition(ResourceLocation path) {
		return getModelContainer(path).modelDefinition;
	}
	

	public static record ModelFileFormatPath(ModelFormat format, String directory, String extension) {}
	public static ModelFileFormatPath[] PATHS = new ModelFileFormatPath[] {
		new ModelFileFormatPath(ModelFormat.GECKO, "geo", ".geo.json"), 
		new ModelFileFormatPath(ModelFormat.GENERIC, "bb", ".bbmodel"), 
	};
	
	@Override
	protected Map<ResourceLocation, LayerDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, LayerDefinition> models = new HashMap<>();

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID)) {
			for (ModelFileFormatPath format : PATHS) {
				String DIR = format.directory() + "/rotp";
				String EXTENSION = format.extension();
				Map<ResourceLocation, Resource> resources = resourceManager.listResources(DIR, path -> path.getPath().endsWith(EXTENSION));
				for (var resourceEntry : resources.entrySet()) {
					ResourceLocation resourcePathFull = resourceEntry.getKey();
					ResourceLocation modelPath = resourcePathFull.withPath(
							StringUtil.trimEnding(resourceEntry.getKey().getPath(), EXTENSION).substring(DIR.length() + 1));
					JsonElement json = null;
					try (var reader = resourceEntry.getValue().openAsReader()) {
						json = JSONUtil.parse(reader);
					}
					catch (IOException e) {
						JojoMod.getLogger().error("Failed to parse model {}", modelPath, e);
					}
					if (json != null) {
						LayerDefinition model = ParseModEntityModel.parse(json, format.format());
						models.put(modelPath, model);
					}
				}
			}
		}
		
		return models;
	}
	
	@Override
	protected void apply(Map<ResourceLocation, LayerDefinition> modelsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		for (var oldModel : this.models.values()) {
			oldModel.reset();
		}
		for (var readEntry : modelsRead.entrySet()) {
			ResourceLocation key = readEntry.getKey();
			ResourceModelEntry modelEntry = this.models.computeIfAbsent(key, ResourceModelEntry::new);
			modelEntry.onModelLoad(readEntry.getValue());
		}
		JojoMod.getLogger().info("Loaded {} models", modelsRead.size());
		listeners.forEach(listener -> listener.accept(this));
	}

}
