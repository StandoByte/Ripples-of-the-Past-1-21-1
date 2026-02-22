package com.github.standobyte.jojo.client.shader.core;

import com.mojang.blaze3d.pipeline.MainTarget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public abstract class RotpShader {

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
	
	public abstract void onResourceReload(ResourceManager resourceManager);
	public void loadCoreShaders(RegisterShadersEvent event) {}
	public abstract void resize(int width, int height);
	public abstract void close();
	
	public void frameRenderCallback(RenderLevelStageEvent event) {}
}
