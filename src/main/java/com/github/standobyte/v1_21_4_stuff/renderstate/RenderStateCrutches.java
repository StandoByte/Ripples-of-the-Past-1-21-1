package com.github.standobyte.v1_21_4_stuff.renderstate;

import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public class RenderStateCrutches {
	public static LivingEntityRenderState currentEntityRenderState;
	public static StandEntityRenderState currentStandEntityRenderState;

	public static void beforeLivingRender(LivingEntity entity, HumanoidRenderState reusedState, 
			LivingEntityRenderer<?, ?> renderer, EntityRenderDispatcher entityRenderDispatcher, float partialTick) {
		LivingEntityRenderState.extract(entity, reusedState, renderer, entityRenderDispatcher, partialTick);
		HumanoidRenderState.extractHumanoidRenderState(entity, reusedState, partialTick);
		RipplesPlayerRenderState.extract(entity, reusedState, ((RipplesRenderStateExtensionMixin) reusedState).get(), partialTick);
        RenderStateCrutches.currentEntityRenderState = reusedState;
	}
	
	public static void afterLivingRender() {
        RenderStateCrutches.currentEntityRenderState = null;
	}
}
