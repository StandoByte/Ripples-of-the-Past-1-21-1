package com.github.standobyte.jojo.client.shader;

import java.util.SequencedMap;

import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.jojo.client.shader.core.BufferWithSource;
import com.github.standobyte.jojo.client.shader.core.RotpShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class StandTranslucencyShader extends RotpShader {
	protected boolean usedThisFrame;
	protected PostChain glslShaderChain;
	protected BufferWithSource frameBuffer;

	public StandTranslucencyShader(Minecraft mc, SequencedMap<RenderType, ByteBufferBuilder> fixedRenderBuffers) {
		frameBuffer = new BufferWithSource(createMainTargetBuffer(mc));
		frameBuffer.initSource(new CustomMultiBufferSource(
				new ByteBufferBuilder(786432), 
				fixedRenderBuffers, 
				frameBuffer.createTargetShard("stand_translucent")));
	}
	
	@Override
	public void loadPostShader(ResourceManager resourceManager) {
		closePostChain();
		glslShaderChain = ModShaders.loadPostShaderChain(JojoMod.resLoc("fp_stand_translucent"), frameBuffer.buffer);
	}
	
	protected void closePostChain() {
		if (glslShaderChain != null) {
			glslShaderChain.close();
			glslShaderChain = null;
		}
	}
	
	@Override
	public void resize(int width, int height) {
		// for whatever reason, if you resize them in the opposite order, and use the fullscreen button, it breaks
		frameBuffer.resizeBuffer(width, height);
		if (glslShaderChain != null) {
			glslShaderChain.resize(width, height);
		}
	}
	
	@Override
	public void close() {
		closePostChain();
		frameBuffer.destroyBuffers();
	}
	
	
	
	public MultiBufferSource useBufferSource(MultiBufferSource original) {
		if (glslShaderChain != null) {
			usedThisFrame = true;
			return frameBuffer.bufferSource;
		}
		return original;
	}
	
	@Override
	public void frameRenderCallback(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (isBeforeEntities(stage)) {
			// clear the frame
			this.usedThisFrame = false;
			frameBuffer.clearBuffer();
		}
		
		// then entities render
		
		else if (this.usedThisFrame) {
			if (isLastInLevelRender(stage)) {
				// render the frame and apply the shader
				Minecraft mc = Minecraft.getInstance();
				frameBuffer.copyDepthFrom(mc.getMainRenderTarget());
				
				endBatch((MultiBufferSource.BufferSource) this.frameBuffer.bufferSource);
				
				if (glslShaderChain != null) {
					RenderSystem.disableBlend();
					RenderSystem.disableDepthTest();
					RenderSystem.resetTextureMatrix();
					// glslShaderChain.process(frameBuffer, EntityShaders.resourcePoolCache);
					glslShaderChain.process(mc.getTimer().getGameTimeDeltaTicks());
				}
				
				mc.getMainRenderTarget().bindWrite(true);
			}
			
			else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				// blend the resulting frame onto the screen
				Minecraft mc = Minecraft.getInstance();
				
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
						GlStateManager.SourceFactor.ZERO,
						GlStateManager.DestFactor.ONE
						);
				// frameBuffer.blitAndBlendToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
				frameBuffer.buffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
				RenderSystem.disableBlend();
				RenderSystem.defaultBlendFunc();
			}
		}
	}
	
}
