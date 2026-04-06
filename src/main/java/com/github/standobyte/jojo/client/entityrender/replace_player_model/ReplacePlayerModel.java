package com.github.standobyte.jojo.client.entityrender.replace_player_model;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.event.client.ReplacePlayerModelEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ReplacePlayerModel {
	public PlayerModel model = null;
	public ResourceLocation texture = null;
	public RotpAnimDefinition animation = null;
	public Predicate<RenderLayer> rendererLayerFilter = null;
	public StandSkin standSkin = null;
	public ResourceLocation context = null;

	public static void afterEvent(ReplacePlayerModelEvent event) {
		int entityId = event.entity.getId();
		if (
				event.replacingModel != null || 
				event.texture != null || 
				event.animation != null) {
			ReplacePlayerModel replacement = byEntityId.computeIfAbsent(entityId, __ -> new ReplacePlayerModel());
			replacement.model = event.replacingModel;
			replacement.texture = event.texture;
			replacement.animation = event.animation;
			
			replacement.rendererLayerFilter = event.rendererLayerFilter;
			replacement.standSkin = event.standSkin;
			replacement.context = event.context;
		}
		else {
			ReplacePlayerModel replacement = byEntityId.get(entityId);
			if (replacement != null) {
				replacement.model = null;
				replacement.texture = null;
				replacement.animation = null;
				
				replacement.rendererLayerFilter = null;
				replacement.standSkin = null;
				replacement.context = null;
			}
		}
	}
	
	
	@Nullable
	public static ReplacePlayerModel get(Entity entity) {
		return byEntityId.get(entity.getId());
	}
	
	@Nullable
	public static PlayerModel getModel(Entity entity) {
		ReplacePlayerModel replacement = byEntityId.get(entity.getId());
		return replacement != null ? replacement.model : null;
	}

	@Nullable
	public static ResourceLocation getTexture(Entity entity) {
		ReplacePlayerModel replacement = byEntityId.get(entity.getId());
		return replacement != null ? replacement.texture : null;
	}
	
	@Nullable
	public static Predicate<RenderLayer> getRendererLayerFilter(Entity entity) {
		ReplacePlayerModel replacement = byEntityId.get(entity.getId());
		return replacement != null ? replacement.rendererLayerFilter : null;
	}
	
	public static Int2ObjectMap<ReplacePlayerModel> byEntityId = new Int2ObjectArrayMap<>();
	
}
