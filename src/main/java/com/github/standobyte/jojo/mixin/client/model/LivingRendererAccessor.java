package com.github.standobyte.jojo.mixin.client.model;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public interface LivingRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {
	@Invoker("setupRotations") void invokeSetupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale);
	@Invoker("scale") void invokeScale(T livingEntity, PoseStack poseStack, float partialTickTime);
	@Invoker("getRenderType") RenderType invokeGetRenderType(T livingEntity, boolean bodyVisible, boolean translucent, boolean glowing);
}
