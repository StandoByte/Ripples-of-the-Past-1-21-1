package com.github.standobyte.jojo.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.CustomLevelRenderStages;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.client.ClientHooks;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow @Final Minecraft minecraft;

	@Inject(method = "renderItemInHand", at = @At("TAIL"))
	public void dispatchRenderStage(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
		ClientHooks.dispatchRenderStage(CustomLevelRenderStages.AFTER_HAND_RENDER, minecraft.levelRenderer, null, 
				projectionMatrix, projectionMatrix, 
				minecraft.levelRenderer.getTicks(), camera, minecraft.levelRenderer.getFrustum());
	}
}
