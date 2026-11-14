package com.github.standobyte.jojo.client.entityrender.entitylayers;

import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;

// in case we port to next versions in the future, this should make it like 0.1% easier
public abstract class RenderLayer2<T extends Entity, S extends EntityRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {

	public RenderLayer2(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
			float ticks, float headYRotation, float headXRotation) {
    	if (RenderStateCrutches.currentEntityRenderState != null) {
    		render(poseStack, buffer, 
    				packedLight, (S) RenderStateCrutches.currentEntityRenderState, 
    				headYRotation, headXRotation);
    	}
	}

	public abstract void render(PoseStack poseStack, MultiBufferSource buffer, 
			int packedLight, S renderState, 
			float headYRotation, float headXRotation);
}
