package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader.ModelFileFormatPath;
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

public class ClothesModelLoader extends SimplePreparableReloadListener<Map<ResourceLocation, LayerDefinition>> {
	private static ClothesModelLoader instance;
	
	@ApiStatus.Internal
	public static void init(/*AddClientReloadListenersEvent*/RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new ClothesModelLoader();
		}
//		event.addListener(JojoMod.resLoc("clothes"), instance);
		event.registerReloadListener(instance);
	}
	
	public static ClothesModelLoader getInstance() {
		return instance;
	}
	
	
	private Map<ResourceLocation, ClothesModelEntry> models = new HashMap<>();
	
	public ClothesModelEntry getClothesModelEntry(ResourceLocation path) {
		return models.get(path);
	}
	

	@Override
	protected Map<ResourceLocation, LayerDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		Map<ResourceLocation, LayerDefinition> models = new HashMap<>();

		try (Zone zone = _ProfilerFiller.zone(profiler, JojoMod.MOD_ID + "_clothes")) {
			for (ModelFileFormatPath format : RotpGeckoModelLoader.PATHS) {
				String DIR = format.directory() + "/clothes";
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
						JojoMod.getLogger().error("Failed to parse clothes model {}", modelPath, e);
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
	protected void apply(Map<ResourceLocation, LayerDefinition> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.models.clear();
		skinsRead.forEach((key, modelDef) -> this.models.put(key, new ClothesModelEntry(key, modelDef)));
		JojoMod.getLogger().info("Loaded {} clothes models", this.models.size());
	}

}