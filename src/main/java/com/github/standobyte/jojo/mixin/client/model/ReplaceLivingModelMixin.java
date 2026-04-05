package com.github.standobyte.jojo.mixin.client.model;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.world.entity.Entity;

/* The name is just as a reminder that it's supposed to work alongside ReplacePlayerModelMixin, 
 * the real purpose is to disable some layers from rendering.
 */
@Mixin(LivingEntityRenderer.class)
public class ReplaceLivingModelMixin {
	@Shadow protected EntityModel model;

	@WrapWithCondition(method = "render", at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "I"
					+ "Lnet/minecraft/world/entity/Entity;"
					+ "FFFFFF"
					+ ")V"))
	private boolean disableLayersWithCustomModel(RenderLayer layer,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int packedLight,
			Entity livingEntity,
			float limbSwing,
			float limbSwingAmount,
			float partialTick,
			float ageInTicks,
			float netHeadYaw,
			float headPitch) {
		if (isUsingCustomModel()) {
			Class<?> layerClass = layer.getClass();
			return layer instanceof StuckInBodyLayer
					|| layer instanceof ItemInHandLayer
					|| layerClass == SpinAttackEffectLayer.class;
		}
		return true;
	}
	
	@Unique
	protected boolean isUsingCustomModel() {
		return false;
	}
	
}
