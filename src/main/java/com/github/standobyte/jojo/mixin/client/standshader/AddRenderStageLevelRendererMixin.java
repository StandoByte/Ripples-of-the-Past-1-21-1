package com.github.standobyte.jojo.mixin.client.standshader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.CustomLevelRenderStages;
import com.github.standobyte.jojo.client.shader.ModShaders;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public class AddRenderStageLevelRendererMixin {

	@Inject(method = "doEntityOutline", at = @At("TAIL"))
	public void addRenderStage(CallbackInfo ci) {
		ModShaders rotpShaders = ModShaders.getInstance();
		if (rotpShaders != null) {
			rotpShaders.frameRenderCallback(CustomLevelRenderStages.BEFORE_SPECTATOR_SHADER);
		}
	}
}
