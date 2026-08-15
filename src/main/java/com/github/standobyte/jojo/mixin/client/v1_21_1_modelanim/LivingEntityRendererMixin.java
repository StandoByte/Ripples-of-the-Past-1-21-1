package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityrender.RenderPlayerSpecial;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Shadow EntityModel<?> model;

    @Inject(method = "render", at = @At(
	    		value = "INVOKE", 
	    		target = "Lnet/minecraft/client/model/EntityModel;setupAnim("
	    				+ "Lnet/minecraft/world/entity/Entity;FFFFF"
	    				+ ")V"))
	public void jojo_ripples$beforeVanillaAnimSetup(LivingEntity entity, float entityYaw, float partialTick, 
			PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
    	RenderPlayerSpecial.beforeVanillaModelAnim(model);
    }

    @Inject(method = "render", at = @At(
	    		value = "INVOKE", 
	    		target = "Lnet/minecraft/client/model/EntityModel;setupAnim("
	    				+ "Lnet/minecraft/world/entity/Entity;FFFFF"
	    				+ ")V",
				shift = Shift.AFTER))
	public void jojo_ripples$afterVanillaAnimSetup(LivingEntity entity, float entityYaw, float partialTick, 
			PoseStack poseStack, MultiBufferSource bufferSource, int light, CallbackInfo ci) {
    	RenderPlayerSpecial.afterVanillaModelAnim(model);
	}

}
