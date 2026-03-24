package com.github.standobyte.jojo.mixin.client.standshader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.shader.ModShaders;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow @Final private Window window;

	@Inject(method = "resizeDisplay", at = @At("RETURN"))
	public void jojo_ripples$onResize(CallbackInfo ci) {
		int width = this.window.getWidth();
		int height = this.window.getHeight();
		
		ModShaders shaders = ModShaders.getInstance();
		if (shaders != null) shaders.resize(width, height);
	}
}
