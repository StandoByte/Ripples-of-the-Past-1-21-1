package com.github.standobyte.jojo.mixin.client.modelanim;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.entityanim.PreFrameEntityAnimCalc;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow private ClientLevel level;

	@Inject(method = "renderLevel", at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
			args =  { "ldc=entities" }))
	public void beforeEntitiesRender(DeltaTracker deltaTracker, boolean renderBlockOutline, 
			Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, 
			Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		PreFrameEntityAnimCalc.onBeforeEntitiesRender(level);
	}
}
