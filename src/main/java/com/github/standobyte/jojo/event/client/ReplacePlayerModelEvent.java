package com.github.standobyte.jojo.event.client;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.ReplacePlayerModel;
import com.github.standobyte.jojo.client.standskin.StandSkin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class ReplacePlayerModelEvent extends Event {
	public final LivingEntity entity;
	public final LivingEntityRenderer<?, ? extends PlayerModel<?>> renderer;
	public final float partialTick;
	
	@Nullable public PlayerModel replacingModel;
	@Nullable public ResourceLocation texture;
	@Nullable public RotpAnimDefinition animation;
	public LivingAnimState animVariables;
	
	@Nullable public Predicate<RenderLayer> rendererLayerFilter;
	
	@Nullable public StandSkin standSkin;
	@Nullable public ResourceLocation context;
	
	public ReplacePlayerModelEvent(LivingEntity entity, 
			LivingEntityRenderer<?, ? extends PlayerModel<?>> renderer, 
			float partialTick, LivingAnimState animVariables) {
		this.entity = entity;
		this.renderer = renderer;
		this.partialTick = partialTick;
		this.animVariables = animVariables;
	}
	
	public LivingEntity getEntity() { 
		return entity;
	}
	
	public LivingEntityRenderer<?, ? extends PlayerModel<?>> getRenderer() { 
		return renderer;
	}
	
	public float getPartialTick() { 
		return partialTick;
	}
	
	public void setModel(PlayerModel model) { 
		this.replacingModel = model;
	}
	
	public void setTexture(ResourceLocation texture) { 
		this.texture = texture;
	}
	
	public void setAnimation(RotpAnimDefinition animation) { 
		this.animation = animation;
	}
	
	public LivingAnimState getAnimVariablesToEdit() { 
		return animVariables;
	}
	
	public void setRendererLayerFilter(Predicate<RenderLayer> filter) {
		this.rendererLayerFilter = filter;
	}
	
	
	public final void afterEvent() {
		ReplacePlayerModel.afterEvent(this);
	}
	
}
