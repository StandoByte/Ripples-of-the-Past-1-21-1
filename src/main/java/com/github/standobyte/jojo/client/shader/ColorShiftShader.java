package com.github.standobyte.jojo.client.shader;

import java.io.IOException;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.CustomLevelRenderStages;
import com.github.standobyte.jojo.client.shader.core.RotpShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class ColorShiftShader extends RotpShader {
	public ColorShiftEffect glslShaderChain;
	@Nullable public ColorShiftEffect.Parameters parameters;

	public ColorShiftShader() {}

	@Override
	public void loadPostShader(ResourceManager resourceManager) {
		closePostChain();

		Minecraft mc = Minecraft.getInstance();
		ResourceLocation path = JojoMod.resLoc("color_shift");
		RenderTarget targetBuffer = mc.getMainRenderTarget();
		try {
			glslShaderChain = new ColorShiftEffect(mc.getTextureManager(), mc.getResourceManager(), targetBuffer, path);
			glslShaderChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		} catch (JsonSyntaxException e) {
			JojoMod.getLogger().error("Failed to load shader: {}", path, e);
			glslShaderChain = null;
		} catch (IOException e) {
			JojoMod.getLogger().error("Failed to parse shader: {}", path, e);
			glslShaderChain = null;
		}
	}

	protected void closePostChain() {
		if (glslShaderChain != null) {
			glslShaderChain.close();
			glslShaderChain = null;
		}
	}

	@Override
	public void resize(int width, int height) {
		if (glslShaderChain != null) {
			glslShaderChain.resize(width, height);
		}
	}

	@Override
	public void close() {
		closePostChain();
	}



	@Override
	public void frameRenderCallback(RenderLevelStageEvent.Stage stage) {
		if (parameters == null) return;
		
		if (glslShaderChain != null && stage == CustomLevelRenderStages.BEFORE_SPECTATOR_SHADER) {
			Minecraft mc = Minecraft.getInstance();

			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.resetTextureMatrix();
			glslShaderChain.process(parameters, mc.getTimer().getGameTimeDeltaTicks());
			mc.getMainRenderTarget().bindWrite(true);
		}
	}

}
