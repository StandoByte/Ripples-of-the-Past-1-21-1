package com.github.standobyte.jojo.client.shader.core;

import java.io.IOException;
import java.util.Iterator;

import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.pipeline.MainTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public abstract class RotpShader {
	
	public abstract void loadPostShader(ResourceManager resourceManager);
	public void loadCoreShaders(RegisterShadersEvent event) {}
	public abstract void resize(int width, int height);
	public abstract void close();
	public void frameRenderCallback(RenderLevelStageEvent event) {}

	public static MainTarget createMainTargetBuffer(Minecraft mc) {
		MainTarget frameBuffer = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight()/*, false*/);
		frameBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		frameBuffer.clear(Minecraft.ON_OSX);
		return frameBuffer;
	}
	
	public static void endBatch(MultiBufferSource.BufferSource bufferSource) {
//		bufferSource.endLastBatch();
//		bufferSource.endBatch(Sheets.translucentCullBlockSheet());
//		bufferSource.endBatch(Sheets.bannerSheet());
//		bufferSource.endBatch(Sheets.shieldSheet());
//		bufferSource.endBatch(RenderType.armorEntityGlint());
//		bufferSource.endBatch(RenderType.glint());
//		bufferSource.endBatch(RenderType.glintTranslucent());
//		bufferSource.endBatch(RenderType.entityGlint());
//		bufferSource.endBatch(RenderType.entityGlintDirect());
//		bufferSource.endBatch(RenderType.waterMask());
		bufferSource.endBatch();
	}
	
	public static boolean isBeforeEntities(RenderLevelStageEvent.Stage stage) { return stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS; }
	public static boolean isLastInLevelRender(RenderLevelStageEvent.Stage stage) { return stage == RenderLevelStageEvent.Stage.AFTER_WEATHER; }
	
	protected static ResourceProvider getResourceProvider() {
		return Minecraft.getInstance().getResourceManager();
	}
	
	protected static void modifyShader(PostChain loadedShaderChain, PostPassConsumer a) {
		if (loadedShaderChain != null) {
			Iterator<PostPass> passIter = loadedShaderChain.passes.listIterator();
			while (passIter.hasNext()) {
				PostPass pass = passIter.next();
				try {
					a.accept(pass);
				}
				catch (IOException e) {
					JojoMod.getLogger().error("", e);
				}
			}
		}
	}
	
	public static interface PostPassConsumer {
		void accept(PostPass pass) throws IOException;
	}
	
}
