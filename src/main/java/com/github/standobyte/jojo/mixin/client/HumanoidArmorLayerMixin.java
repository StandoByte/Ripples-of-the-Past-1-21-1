package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ModEntityTypeRenderers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void cancelArmorRender(
			PoseStack poseStack,
			MultiBufferSource buffer,
			int packedLight,
			LivingEntity livingEntity,
			float limbSwing,
			float limbSwingAmount,
			float partialTicks,
			float ageInTicks,
			float netHeadYaw,
			float headPitch,
			CallbackInfo ci) {
		if (ModEntityTypeRenderers.cancelRenderArmor(livingEntity)) {
			ci.cancel();
		}
	}

}
