package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.barrage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityrender.RenderPlayerSpecial;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

	@Inject(method = "render", at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "getBuffer("
					+ "Lnet/minecraft/client/renderer/RenderType;"
					+ ")Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
	public void jojo_ripples$setupBarrageSwingsRender(LivingEntity entity, float entityYaw, float partialTicks, 
			PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		RenderPlayerSpecial.setupBarrageSwings();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", 
	target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;"
			+ "render("
//			+ "Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
			+ "Lnet/minecraft/world/entity/Entity;FF"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "I)V"))
	public void jojo_ripples$stopBarrageSwingsRender(LivingEntity entity, float entityYaw, float partialTicks, 
			PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, CallbackInfo ci) {
		BarrageSwings.setupToRender(null);
	}

}
