package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(value = GameRenderer.class)
public class GameRendererPartialTickMixin {
	@Shadow @Final Minecraft minecraft;

	@ModifyVariable(method = "renderItemInHand", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float armRenderChangePartialTick(float partialTick) {
		if (TimeStopClientState.isCameraEntityFrozen) {
			return 1;
		}
		return partialTick;
	}
	
}
