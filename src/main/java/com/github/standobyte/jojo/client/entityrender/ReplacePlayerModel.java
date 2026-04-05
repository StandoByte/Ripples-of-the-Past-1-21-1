package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.event.client.ReplacePlayerModelEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ReplacePlayerModel {
	public PlayerModel model = null;
	public ResourceLocation texture = null;
	public RotpAnimDefinition animation = null;
	
	public void set(PlayerModel model, ResourceLocation texture, RotpAnimDefinition animation) {
		this.model = model;
		this.texture = texture;
		this.animation = animation;
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
	
	public static Int2ObjectMap<ReplacePlayerModel> byEntityId = new Int2ObjectArrayMap<>();

	public static void afterEvent(ReplacePlayerModelEvent event) {
		int entityId = event.entity.getId();
		if (event.replacingModel != null || event.texture != null || event.animation != null) {
			ReplacePlayerModel replacement = byEntityId.computeIfAbsent(entityId, __ -> new ReplacePlayerModel());
			replacement.set(event.replacingModel, event.texture, event.animation);
		}
		else {
			ReplacePlayerModel replacement = byEntityId.get(entityId);
			if (replacement != null) {
				replacement.set(null, null, null);
			}
		}
	}
	
}
