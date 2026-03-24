package com.github.standobyte.jojo.client.shader;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedMap;

import com.github.standobyte.jojo.client.shader.core.BufferWithSource;
import com.github.standobyte.jojo.client.shader.core.RotpShader;
import com.github.standobyte.jojo.client.shader.standaura.AuraUtil;
import com.github.standobyte.jojo.client.shader.standaura.AuraUtil.ResolveAuraVars;
import com.github.standobyte.jojo.client.shader.standaura.BufferSourceRecolor;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;

// FIXME (stand aura shader) crash (Not building!) when i equip an enchanted item
// FIXME (stand aura shader) stand rendering breaks completely
// FIXME (stand aura shader) depth test
public class StandAuraShader extends RotpShader {
	protected boolean usedThisFrame;
	protected PostChain glslShaderChain;
	protected boolean _renderingNow;
	
	protected BufferWithSource frameBuffer;
	protected BufferSourceRecolor auraColor;
	
	protected BufferWithSource silhouetteBuffer;
	protected BufferSourceRecolor silhouetteColor;
	
	protected ShaderInstance outlineTranslucentShader;
	public final RenderStateShard.ShaderStateShard TRANSLUCENT_OUTLINE_SHADER = new RenderStateShard.ShaderStateShard(() -> this.outlineTranslucentShader);

	public StandAuraShader(Minecraft mc, SequencedMap<RenderType, ByteBufferBuilder> fixedRenderBuffers) {
		frameBuffer = new BufferWithSource(createMainTargetBuffer(mc));
		frameBuffer.buffer.setClearColor(1, 1, 1, 0); // white color, makes the border brighter kinda
		auraColor = new BufferSourceRecolor(
				new ByteBufferBuilder(MAGIC_BUFFER_CAPACITY), 
				fixedRenderBuffers, 
				frameBuffer.createTargetShard("stand_aura"), 
				TRANSLUCENT_OUTLINE_SHADER, 
				RenderStateShard.TRANSLUCENT_TRANSPARENCY, 
				DISABLE_DEPTH_TEST);
		frameBuffer.initSource(auraColor);
		
		silhouetteBuffer = new BufferWithSource(createMainTargetBuffer(mc));
		silhouetteColor = new BufferSourceRecolor(
				new ByteBufferBuilder(MAGIC_BUFFER_CAPACITY), 
				fixedRenderBuffers, 
				silhouetteBuffer.createTargetShard("stand_aura_silhouette"), 
				RenderStateShard.RENDERTYPE_OUTLINE_SHADER);
		silhouetteBuffer.initSource(silhouetteColor);
		
		NeoForge.EVENT_BUS.register(this);
		
	}
	
	@Deprecated
	public static final RenderStateShard DISABLE_DEPTH_TEST = new RenderStateShard("no_depth_test", 
			() -> { RenderSystem.disableDepthTest(); },
			() -> {}) {};
	
	public static int NOISE_V_SHIFT_LOOP = 60;
	@Override
	public void loadPostShader(ResourceManager resourceManager) {
		closePostChain();
		
		glslShaderChain = ModShaders.loadPostShaderChain(JojoMod.resLoc("stand_aura"), frameBuffer.buffer);
		modifyShader(glslShaderChain, (PostPass pass) -> {
			String effectName = pass.effect.getName();
			switch (effectName) {
				case "jojo_ripples:aura_apply_noise_mask" -> {
					pass.effect.close();
					pass.effect = new EffectInstance(getResourceProvider(), effectName) {
						@Override
						public void apply() {
							this.setSampler("SilhouetteSampler", StandAuraShader.this.silhouetteBuffer.buffer::getColorTextureId);
							this.safeGetUniform("NoiseVShift").set(ClientUtil.getTime(true) % NOISE_V_SHIFT_LOOP / NOISE_V_SHIFT_LOOP);
							super.apply();
						}
					};
				}
			}
		});
	}
	
	@Override
	public void loadCoreShaders(RegisterShadersEvent event) {
		ModShaders.loadCoreShader(event, 
				JojoMod.resLoc("outline_translucent"), DefaultVertexFormat.POSITION_TEX_COLOR, 
				shader -> outlineTranslucentShader = shader);
	}
			
	protected void closePostChain() {
		if (glslShaderChain != null) {
			glslShaderChain.close();
			glslShaderChain = null;
		}
	}
	
	@Override
	public void resize(int width, int height) {
		frameBuffer.resizeBuffer(width, height);
		silhouetteBuffer.resizeBuffer(width, height);
		if (glslShaderChain != null) {
			glslShaderChain.resize(width, height);
		}
	}
	
	@Override
	public void close() {
		closePostChain();
		frameBuffer.destroyBuffers();
		silhouetteBuffer.destroyBuffers();
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
		if (!ClientModSettings.getSettingsReadOnly().standAura) return;
		
		RenderLevelStageEvent.Stage stage = event.getStage();
		if (isBeforeEntities(stage)) {
			this.usedThisFrame = false;
			frameBuffer.clearBuffer();
			silhouetteBuffer.clearBuffer();
			entitiesToRender.clear();
		}
		
		// then entities render
		
		else if (isLastInLevelRender(stage)) {
			renderAuraOnEntities();
			if (this.usedThisFrame) {
				Minecraft mc = Minecraft.getInstance();

				endBatch((MultiBufferSource.BufferSource) this.frameBuffer.bufferSource);
				endBatch((MultiBufferSource.BufferSource) this.silhouetteBuffer.bufferSource);

				if (glslShaderChain != null) {
					RenderSystem.disableBlend();
					RenderSystem.disableDepthTest();
					RenderSystem.resetTextureMatrix();
					glslShaderChain.process(mc.getTimer().getGameTimeDeltaTicks());
				}

				mc.getMainRenderTarget().bindWrite(true);
			}
		}
		
		else if (this.usedThisFrame) {
			if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				Minecraft mc = Minecraft.getInstance();
				
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
						GlStateManager.SourceFactor.ZERO,
						GlStateManager.DestFactor.ONE
						);
				frameBuffer.buffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
				RenderSystem.disableBlend();
				RenderSystem.defaultBlendFunc();
			}
		}
	}



	protected List<EntityAuraColor> entitiesToRender = new ArrayList<>();
	protected static record EntityAuraColor(LivingEntity entity, 
			LivingEntityRenderer renderer, float partialTick, PoseStack.Pose pose, 
			int auraColor, float auraInflate) {}
	
	@SubscribeEvent
	public <T extends LivingEntity, M extends EntityModel<T>> void afterEntityRender(RenderLivingEvent.Post<T, M> event) {
		if (this._renderingNow) return;
		
		LivingEntity entity = event.getEntity();
		ResolveAuraVars color = AuraUtil.getStandAuraColor(entity);
		if (color != null) {
			color.color &= 0xFFFFFF;
			entitiesToRender.add(new EntityAuraColor(entity, 
					event.getRenderer(), event.getPartialTick(), event.getPoseStack().last().copy(), 
					color.color, color.inflate));
		}
	}
	
	protected void renderAuraOnEntities() {
		if (!entitiesToRender.isEmpty()) {
			MultiBufferSource source = this.useBufferSource(null);
			if (source == null) return;
			
			this._renderingNow = true;
			Minecraft mc = Minecraft.getInstance();
			PoseStack poseStack = new PoseStack();
			
			this.frameBuffer.copyDepthFrom(mc.getMainRenderTarget());
			this.silhouetteBuffer.copyDepthFrom(mc.getMainRenderTarget());
			for (EntityAuraColor noted : entitiesToRender) {
				LivingEntity entity = noted.entity;
				LivingEntityRenderer renderer = noted.renderer;
				int color = noted.auraColor;
				float partialTick = noted.partialTick;
				
				poseStack.poseStack.addLast(noted.pose);
				
				float entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
				
				poseStack.pushPose();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				for (float inflate = 5; inflate >= 1; inflate--) {
					AuraUtil.inflateEachCube = inflate + noted.auraInflate;
					float alphaAdditive = 0.05f;
					this.auraColor.setColor(FastColor.ARGB32.color(FastColor.as8BitChannel(alphaAdditive), color));
					// render
					renderer.render(entity, entityYaw, partialTick, poseStack, source, ClientUtil.MAX_LIGHT);
				}
				poseStack.popPose();
				
				AuraUtil.inflateEachCube = null;
				this.silhouetteColor.setColor(FastColor.ARGB32.color(255, color));
				renderer.render(entity, entityYaw, partialTick, poseStack, this.silhouetteBuffer.bufferSource, ClientUtil.MAX_LIGHT);
				
				poseStack.popPose();
			}
			this._renderingNow = false;
		}
	}

}
