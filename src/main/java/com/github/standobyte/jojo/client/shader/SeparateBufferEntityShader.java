package com.github.standobyte.jojo.client.shader;

import java.util.SequencedMap;
import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.github.standobyte.v1_21_4_stuff.PostEffectCache;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class SeparateBufferEntityShader {
	protected ResourceLocation postShaderId;
	protected String outputShardName;
	
	public RenderTarget frameBuffer;
	public MultiBufferSource bufferSource;
	
	protected boolean usedThisFrame = false;
	
	public SeparateBufferEntityShader(Minecraft mc, String outputShardName, ResourceLocation postShaderId) {
		this.postShaderId = postShaderId;
		this.outputShardName = outputShardName;
		initTargetBuffer(mc);
	}
	
	protected void initTargetBuffer(Minecraft mc) {
		frameBuffer = createBuffer(mc);
	}
	
	protected static MainTarget createBuffer(Minecraft mc) {
		MainTarget frameBuffer = new MainTarget(mc.getWindow().getWidth(), mc.getWindow().getHeight()/*, false*/);
		frameBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		frameBuffer.clear(Minecraft.ON_OSX);
		return frameBuffer;
	}
	
	protected List<RenderStateShard> renderTypeModification() {
		RenderStateShard.OutputStateShard targetShard = createTargetShard(outputShardName, frameBuffer);
		List<RenderStateShard> list = new ArrayList<>();
		list.add(targetShard);
		return list;
	}

	protected static RenderStateShard.OutputStateShard createTargetShard(String name, RenderTarget buffer) {
		return new RenderStateShard.OutputStateShard(
				name, 
				() -> buffer.bindWrite(false), 
				() -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
	}
	
	protected void createBufferSource(Minecraft mc, RenderBuffers vanillaRenderBuffers) {
		List<RenderStateShard> modification = renderTypeModification();
		SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;
		fixedBuffers = ClientReflection.getFixedBuffers(vanillaRenderBuffers.bufferSource());
		bufferSource = new CustomMultiBufferSource(
				new ByteBufferBuilder(786432), 
				fixedBuffers, modification.toArray(RenderStateShard[]::new));
	}
	
	public void onResourceReload(ResourceManager resourceManager) {}
	
	
	public MultiBufferSource useBufferSourceThisFrame() {
		usedThisFrame = true;
		return bufferSource;
	}
	
	public boolean _renderingNow = false;
	public <T extends LivingEntity, M extends EntityModel<T>> boolean render(LivingEntityRenderer<T, M> renderer, 
			T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource mainBuffer, int packedLight) {
		if (_renderingNow) return false;

		MultiBufferSource source = this.useBufferSourceThisFrame();
		_renderingNow = true;
		renderer.render(entity, entityYaw, partialTicks, poseStack, source, ClientUtil.MAX_LIGHT);
		_renderingNow = false;
		return true;
	}
	
	
	protected void frameRenderCallback(RenderLevelStageEvent event) {
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			frameStart();
		}
		if (this.usedThisFrame) {
			if (stage == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
				this.setupBuffer();
				this.endBatch();
				this.applyEffect();
				Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
			}
			else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				this.blitBuffer();
			}
		}
	}
	
	protected void frameStart() {
		this.usedThisFrame = false;
		frameBuffer.clear(Minecraft.ON_OSX);
	}
	
	protected void setupBuffer() {
		Minecraft mc = Minecraft.getInstance();
		frameBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}
	
	protected void endBatch() {
		MultiBufferSource.BufferSource bufferSource = (MultiBufferSource.BufferSource) this.bufferSource;
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

//	@SuppressWarnings("deprecation")
	protected void applyEffect() {
		Minecraft mc = Minecraft.getInstance();
		PostChain postchain = getShader(mc);
		if (postchain != null) {
			RenderSystem.disableBlend();
			RenderSystem.disableDepthTest();
			RenderSystem.resetTextureMatrix();
//			postchain.process(frameBuffer, EntityShaders.resourcePoolCache);
			postchain.process(mc.getTimer().getGameTimeDeltaTicks());
		}
	}
	
	protected PostChain getShader(Minecraft mc) {
//		return mc.getShaderManager().getPostChain(postShaderId, LevelTargetBundle.MAIN_TARGETS);
		return PostEffectCache.instance.getEffect(postShaderId, frameBuffer, true);
	}
	
	protected void blitBuffer() {
		Minecraft mc = Minecraft.getInstance();
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ZERO,
				GlStateManager.DestFactor.ONE
				);
//		frameBuffer.blitAndBlendToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight());
		frameBuffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}
	
	
	protected void resize(int width, int height) {
		if (frameBuffer != null) {
			frameBuffer.resize(width, height, Minecraft.ON_OSX);
		}
	}
}
