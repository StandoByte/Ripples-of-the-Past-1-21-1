package com.github.standobyte.jojo.mixin.client.standshader;

import java.io.IOException;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.shader.EntityShaders;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(PostChain.class)
public class PostChainMixin {
	@Shadow @Final ResourceProvider resourceProvider;
	@Shadow @Final List<PostPass> passes;

	// tried ModifyVariable, didn't work for whatever reason, using the ol' reliable
	@Inject(method = "addPass", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$customShaderPass(String name, 
			RenderTarget inTarget, RenderTarget outTarget, 
			boolean useLinearFilter, CallbackInfoReturnable<PostPass> ci) {
		try {
			PostPass custom = EntityShaders.createCustomPass(resourceProvider, name, inTarget, outTarget, useLinearFilter);
			if (custom != null) {
				this.passes.add(this.passes.size(), custom);
				ci.setReturnValue(custom);
				EntityShaders.customizePass(custom, resourceProvider, name, inTarget, outTarget, useLinearFilter);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Inject(method = "addPass", at = @At("TAIL"))
	public void jojo_ripples$customShaderEffect(String name, 
			RenderTarget inTarget, RenderTarget outTarget, 
			boolean useLinearFilter, CallbackInfoReturnable<PostPass> ci) {
		PostPass pass = ci.getReturnValue();
		if (pass != null) {
			try {
				EntityShaders.customizePass(pass, resourceProvider, name, inTarget, outTarget, useLinearFilter);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
