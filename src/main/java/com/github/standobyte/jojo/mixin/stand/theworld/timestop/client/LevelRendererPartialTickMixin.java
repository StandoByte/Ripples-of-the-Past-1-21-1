package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class LevelRendererPartialTickMixin {

	@ModifyVariable(method = "renderSnowAndRain", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float weatherChangePartialTick(float partialTick) {
		if (TimeStopClientState.isTimeStopped) {
			return 1;
		}
		return partialTick;
	}

	@ModifyVariable(method = "renderClouds", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float cloudsChangePartialTick(float partialTick) {
		if (TimeStopClientState.isTimeStopped) {
			return 1;
		}
		return partialTick;
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void cancelTickCountIncrement(CallbackInfo ci) {
		if (TimeStopClientState.isTimeStopped) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "renderEntity", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	public float partialTick(float partialTick, 
			Entity entity, double camX, double camY, double camZ, 
			float partialTick_, PoseStack poseStack, MultiBufferSource bufferSource) {
		if (TimeStopClientState.isEntityFrozen(entity)) {
			return TimeStopClientState.partialTick;
		}
		return partialTick;
	}
	
}
