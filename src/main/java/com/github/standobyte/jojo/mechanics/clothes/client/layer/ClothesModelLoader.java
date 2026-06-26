package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
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
		return RotpGeckoModelLoader.parseModels("clothes", resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, LayerDefinition> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.models.clear();
		skinsRead.forEach((key, modelDef) -> this.models.put(key, new ClothesModelEntry(key, modelDef)));
		JojoMod.getLogger().info("Loaded {} clothes models", this.models.size());
	}

}