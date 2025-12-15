package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;

@SuppressWarnings("rawtypes")
@Mixin(AgeableListModel.class)
public abstract class AgeableModelMixinSuperclass extends EntityModel {

	@Inject(method = "renderToBuffer("
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			+ "III)V", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$renderWithBends(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {}

	@Inject(method = "renderToBuffer("
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lcom/mojang/blaze3d/vertex/VertexConsumer;"
			+ "III)V", at = @At("TAIL"))
	public void jojo_ripples$thenRenderBarrageSwings(PoseStack poseStack, VertexConsumer buffer, 
			int packedLight, int packedOverlay, int color, CallbackInfo ci) {}

}
