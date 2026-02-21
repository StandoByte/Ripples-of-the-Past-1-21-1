package com.github.standobyte.jojo.client.shader.standaura;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.shader.EntityShaders;
import com.github.standobyte.jojo.client.shader.SeparateBufferEntityShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;


// FIXME (stand aura shader) crash (Not building!) when i equip an enchanted item
// FIXME (stand aura shader) noise drawing crutch (PowerHud)
// FIXME (stand aura shader) stand rendering breaks completely
// FIXME (stand aura shader) depth test
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class StandAuraEntityShader extends SeparateBufferEntityShader {
	protected BufferSourceRecolor auraColor;
	
	public RenderTarget silhouetteBuffer;
	protected BufferSourceRecolor silhouetteBufferSource;
	
	public RenderTarget noiseBuffer;
	
	public StandAuraEntityShader(Minecraft mc, String outputShardName, ResourceLocation postShaderId) {
		super(mc, outputShardName, postShaderId);
	}
	
	@Override
	protected void initTargetBuffer(Minecraft mc) {
		super.initTargetBuffer(mc);
		silhouetteBuffer = createBuffer(mc);
		
		int width = mc.getWindow().getWidth();
		int height = mc.getWindow().getHeight();
		noiseBuffer = new TextureTarget(width, height, false, Minecraft.ON_OSX);
		noiseBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		noiseBuffer.clear(Minecraft.ON_OSX);
	}
	
	@Override
	protected void createBufferSource(Minecraft mc, RenderBuffers vanillaRenderBuffers) {
		List<RenderStateShard> outlineShards = renderTypeModification();
		outlineShards.add(OUTLINE_TRANSLUCENT_SHADER);
		outlineShards.add(RenderStateShard.TRANSLUCENT_TRANSPARENCY);
		outlineShards.add(DISABLE_DEPTH_TEST);
		auraColor = new BufferSourceRecolor(
				new ByteBufferBuilder(786432), 
				new Object2ObjectLinkedOpenHashMap<>(), 
				outlineShards.toArray(RenderStateShard[]::new));
		bufferSource = auraColor;

		silhouetteBufferSource = new BufferSourceRecolor(
				new ByteBufferBuilder(786432), 
				new Object2ObjectLinkedOpenHashMap<>(), 
				createTargetShard(outputShardName + "_silhouette", silhouetteBuffer), 
				RenderStateShard.RENDERTYPE_OUTLINE_SHADER);
	}

	public static final RenderStateShard.ShaderStateShard OUTLINE_TRANSLUCENT_SHADER = 
			new RenderStateShard.ShaderStateShard(() -> EntityShaders._outlineTranslucentShader);

	public static final RenderStateShard DISABLE_DEPTH_TEST = new RenderStateShard("no_depth_test", 
			() -> { RenderSystem.disableDepthTest(); },
			() -> {}) {};

	
	@Override
	protected void frameRenderCallback(RenderLevelStageEvent event) {
		handleLevelRenderStage(event.getStage());
		super.frameRenderCallback(event);
	}

	public static final ResourceLocation NOISE = JojoMod.resLoc("textures/stand_aura_noise.png");
	@Override
	protected void frameStart() {
		super.frameStart();
		silhouetteBuffer.clear(Minecraft.ON_OSX);
//		noiseBuffer.clear(Minecraft.ON_OSX);
//		drawNoiseTexture();
	}
	
	@Override
	protected void blitBuffer() {
		Minecraft mc = Minecraft.getInstance();
		
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ZERO,
				GlStateManager.DestFactor.ONE
				);
//		noiseBuffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
		frameBuffer.blitToScreen(mc.getWindow().getWidth(), mc.getWindow().getHeight(), false);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}

	@Override
	protected void setupBuffer() {
		super.setupBuffer();
		Minecraft mc = Minecraft.getInstance();
		silhouetteBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}
	
	@Override
	protected void endBatch() {
		super.endBatch();
		((MultiBufferSource.BufferSource) this.silhouetteBufferSource).endBatch();
	}
	
	@Override
	protected void applyEffect() {
		super.applyEffect();
	}
	
	@Override
	protected void resize(int width, int height) {
		super.resize(width, height);
		if (silhouetteBuffer != null) {
			silhouetteBuffer.resize(width, height, Minecraft.ON_OSX);
		}
		
		noiseBuffer.resize(width, height, Minecraft.ON_OSX);
	}

	public void drawNoiseTexture() {
		this.noiseBuffer.clear(Minecraft.ON_OSX);
		this.noiseBuffer.bindWrite(true);
        RenderSystem.setShaderTexture(0, NOISE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		Window window = Minecraft.getInstance().getWindow();
		int windowWidth = (int) (window.getWidth() * window.getGuiScale());
		int windowHeight = (int) (window.getHeight() * window.getGuiScale());
		windowWidth = window.getWidth();
		windowHeight = window.getHeight();

		Matrix4f matrix4f = new Matrix4f();
		BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		float ratio = (float) windowWidth / windowHeight;
		int x1 = 0;
		int x2 = x1 + (int) (windowHeight * ratio);
		float loop = 80f;
		int y1 = (int) (-windowHeight * (EntityShaders.getTime() % loop / loop));
		int y2 = y1 + windowHeight;
		float minU = 0;
		float minV = 0;
		float maxU = ratio;
		float maxV = 1;
		float _x1 = x1;
		float _x2 = x2;
		float _y1 = y1;
		float _y2 = y2;
		bufferbuilder.addVertex(matrix4f, _x1, _y1, -90).setUv(minU, minV);
		bufferbuilder.addVertex(matrix4f, _x1, _y2, -90).setUv(minU, maxV);
		bufferbuilder.addVertex(matrix4f, _x2, _y2, -90).setUv(maxU, maxV);
		bufferbuilder.addVertex(matrix4f, _x2, _y1, -90).setUv(maxU, minV);
		_y1 = y2;
		_y2 = y2 + windowHeight;
		bufferbuilder.addVertex(matrix4f, _x1, _y1, -90).setUv(minU, minV);
		bufferbuilder.addVertex(matrix4f, _x1, _y2, -90).setUv(minU, maxV);
		bufferbuilder.addVertex(matrix4f, _x2, _y2, -90).setUv(maxU, maxV);
		bufferbuilder.addVertex(matrix4f, _x2, _y1, -90).setUv(maxU, minV);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
		
		this.noiseBuffer.unbindWrite();
	}
	
	protected void handleLevelRenderStage(RenderLevelStageEvent.Stage stage) {
		if (stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
			entitiesToRender.clear();
		}
		else if (stage == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
			renderAuraOnEntities();
		}
	}
	
	protected static record EntityAuraColor(LivingEntity entity, 
			LivingEntityRenderer renderer, float partialTick, PoseStack.Pose pose, 
			int auraColor) {}
	protected List<EntityAuraColor> entitiesToRender = new ArrayList<>();
	
	@SubscribeEvent
	public static <T extends LivingEntity, M extends EntityModel<T>> void afterEntityRender(RenderLivingEvent.Post<T, M> event) {
		StandAuraEntityShader shader = EntityShaders.standAura;
		if (shader._renderingNow) return;
		
		LivingEntity entity = event.getEntity();
		int color = AuraUtil.getStandAuraColor(entity);
		if (color != -1) {
			color &= 0xFFFFFF;
			EntityShaders.standAura.entitiesToRender.add(new EntityAuraColor(entity, 
					event.getRenderer(), event.getPartialTick(), event.getPoseStack().last().copy(), 
					color));
		}
	}
	
	protected void renderAuraOnEntities() {
		if (!entitiesToRender.isEmpty()) {
			this._renderingNow = true;
			Minecraft mc = Minecraft.getInstance();
			MultiBufferSource source = this.useBufferSourceThisFrame();
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
				// TODO variable stand aura intensity
				for (float inflate = 5; inflate >= 1; inflate--) {
					AuraUtil.inflateEachCube = inflate;
					float alphaAdditive = 0.05f;
					this.auraColor.setColor(FastColor.ARGB32.color(FastColor.as8BitChannel(alphaAdditive), color));
					// render
					renderer.render(entity, entityYaw, partialTick, poseStack, source, ClientUtil.MAX_LIGHT);
				}
				poseStack.popPose();
				
				AuraUtil.inflateEachCube = null;
				this.silhouetteBufferSource.setColor(FastColor.ARGB32.color(255, color));
				renderer.render(entity, entityYaw, partialTick, poseStack, this.silhouetteBufferSource, ClientUtil.MAX_LIGHT);
				
				poseStack.popPose();
			}
			this._renderingNow = false;
		}
	}

}
