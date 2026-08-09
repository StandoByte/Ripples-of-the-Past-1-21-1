package com.github.standobyte.jojo.client.entityanim.player;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.parsemodel.ParseModEntityModel.ModelFormat;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

public class PlayerAnimRigLoad extends SimplePreparableReloadListener<Optional<LayerDefinition>> {
	private static PlayerAnimRigLoad loaderInstance;
	
	@ApiStatus.Internal
	public static void init(RegisterClientReloadListenersEvent event) {
		if (loaderInstance == null) {
			loaderInstance = new PlayerAnimRigLoad();
		}
		event.registerReloadListener(loaderInstance);
	}
	
	public static final ResourceLocation PLAYER_ANIM_MODEL_PATH = JojoMod.resLoc("geo_rotp/player_anim.geo.json");
	private PlayerAnimRigModel hierarchyModel;
	public static PlayerAnimRigModel getModel() {
		return loaderInstance.hierarchyModel;
	}
	
	
	@Override
	protected Optional<LayerDefinition> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		try {
			Optional<Resource> resource = resourceManager.getResource(PLAYER_ANIM_MODEL_PATH);
			if (resource.isPresent()) {
				LayerDefinition model = RotpGeckoModelLoader.fromResource(
						resource.get(), ModelFormat.GECKO, PLAYER_ANIM_MODEL_PATH);
				if (model != null) {
					return Optional.of(model);
				}
			}
			else {
				JojoMod.getLogger().error("Resource {} not found", PLAYER_ANIM_MODEL_PATH);
			}
		}
		catch (Exception e) {
			JojoMod.getLogger().error("Failed to load player anim model", e);
		}
		
		return Optional.empty();
	}
	
	@Override
	protected void apply(Optional<LayerDefinition> parsedModel, ResourceManager resourceManager, ProfilerFiller profiler) {
		this.hierarchyModel = parsedModel.map(modelDef -> new PlayerAnimRigModel(modelDef.bakeRoot())).orElse(null);
	}

}