package com.github.standobyte.jojo.client.shader.core;

import java.io.IOException;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.shader.ModShaders;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

public class PostChain2 extends PostChain {

	public PostChain2(TextureManager textureManager, ResourceProvider resourceProvider, RenderTarget screenTarget, 
			ResourceLocation path) throws IOException, JsonSyntaxException {
		super(textureManager, resourceProvider, screenTarget, path);
	}

	@Override
	public PostPass addPass(String name, RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
		ResourceProvider resourceProvider = getResourceProvider();
		PostPass pass = new PostPass(resourceProvider, name, inTarget, outTarget, useLinearFilter);
		PostChain2.customizePass(pass, resourceProvider, name, inTarget, outTarget, useLinearFilter);
		passes.add(passes.size(), pass);
		return pass;
	}
	
	protected ResourceProvider getResourceProvider() {
		return Minecraft.getInstance().getResourceManager();
	}


	@Deprecated
	@Nullable
	private static void customizePass(PostPass pass, ResourceProvider resourceProvider, String name, 
			RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
		switch (name) {
			case "jojo_ripples:aura_apply_noise_mask" -> {
				pass.effect.close();
				pass.effect = new EffectInstance(resourceProvider, name) {
					@Override
					public void apply() {
						this.setSampler("SilhouetteSampler", ModShaders.getInstance().standAura.silhouetteBuffer.buffer::getColorTextureId);
						this.setSampler("NoiseSampler", ModShaders.getInstance().standAura.noiseBuffer::getColorTextureId);
						super.apply();
					}
				};
			}
		}
	}

}
