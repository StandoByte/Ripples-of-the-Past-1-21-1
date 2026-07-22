package com.github.standobyte.jojo.client.entityanim;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class PlayerRigLoader extends SimplePreparableReloadListener<Map<ResourceLocation, LayerDefinition>> {
	private static PlayerRigLoader instance;
	
	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (instance == null) {
			instance = new PlayerRigLoader();
		}
		event.registerReloadListener(instance);
	}
	
	public static PlayerRigLoader getInstance() {
		return instance;
	}
	
	
	private Map<ResourceLocation, ResourceModelEntry> models = new HashMap<>();
	
	private ResourceModelEntry defaultRig;
	private static ResourceLocation DEFAULT_PATH = JojoMod.resLoc("default");
	public ResourceModelEntry getDefault() {
		if (defaultRig == null) {
			defaultRig = models.computeIfAbsent(DEFAULT_PATH, ResourceModelEntry::new);
		}
		return defaultRig;
	}
	
	public ResourceModelEntry getEntry(ResourceLocation path) {
		return models.computeIfAbsent(path, key -> new ResourceModelEntry(key));
	}
	

	@Override
	protected Map<ResourceLocation, LayerDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return RotpGeckoModelLoader.parseModels("player_rig", resourceManager, profiler);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, LayerDefinition> skinsRead, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.models.clear();
		skinsRead.forEach((key, modelDef) -> {
			ResourceModelEntry modelEntry = getEntry(key);
			modelEntry.onModelLoad(modelDef);
		});
	}

}