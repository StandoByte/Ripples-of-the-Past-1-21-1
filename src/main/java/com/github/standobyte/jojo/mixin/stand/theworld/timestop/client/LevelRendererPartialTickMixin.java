package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;

@Mixin(LevelRenderer.class)
public class LevelRendererPartialTickMixin {

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
