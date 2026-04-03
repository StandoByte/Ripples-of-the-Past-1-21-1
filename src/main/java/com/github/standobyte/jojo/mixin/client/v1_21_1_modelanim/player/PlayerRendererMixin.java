package com.github.standobyte.jojo.mixin.client.v1_21_1_modelanim.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.PlayerRendererCallbacks;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public PlayerRendererMixin(Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
		super(context, model, shadowRadius);
	}

	private HumanoidRenderState jojo_ripples$reusedState = new HumanoidRenderState();

	@Inject(method = "render", at = @At(value = "INVOKE", 
	target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
			+ "render("
			+ "Lnet/minecraft/world/entity/LivingEntity;"
			+ "FF"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "I)V"))
	public void jojo_ripples$extractPlayerRenderState(AbstractClientPlayer entity, float entityYaw, float partialTick, 
			PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
		PlayerRendererCallbacks.beforeLivingRender(entity, jojo_ripples$reusedState, this, entityRenderDispatcher, partialTick);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", 
	target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
			+ "render("
			+ "Lnet/minecraft/world/entity/LivingEntity;"
			+ "FF"
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "I)V", shift = At.Shift.AFTER))
	public void jojo_ripples$resetPlayerRenderState(AbstractClientPlayer entity, float entityYaw, float partialTick, 
			PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
		PlayerRendererCallbacks.afterLivingRender();
	}
	
	
	@Inject(method = "renderHand", at = @At("HEAD"))
	public void jojo_ripples$fix1stPersonArmBend(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, 
			AbstractClientPlayer player, ModelPart rendererArm, ModelPart rendererArmwear, CallbackInfo ci) {
		rendererArm.resetPose();
		rendererArmwear.resetPose();
	}
}
