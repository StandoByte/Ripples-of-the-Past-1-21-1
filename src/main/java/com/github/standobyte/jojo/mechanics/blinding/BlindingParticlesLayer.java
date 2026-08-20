package com.github.standobyte.jojo.mechanics.blinding;

import java.util.Random;
import java.util.stream.Stream;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.CustomLevelRenderStages;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsClass;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsMap;
import com.github.standobyte.jojo.init.ModEntityCustomEffects;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class BlindingParticlesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	public BlindingParticlesLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
			float headPitch) {
		// TODO render in 3rd person
	}
	
	public static Stream<BlindingEffect> getEffects(LivingEntity entity) {
		EntityCustomEffectsMap<EntityCustomEffect> effects = EntityCustomEffectsClass.getCustomEffects(entity, false);
		if (effects != null) {
			return effects.getEffects().stream()
					.filter(effect -> effect.effectType == ModEntityCustomEffects.BLINDING_BLOOD_OR_SAND.get())
					.map(effect -> (BlindingEffect) effect);
    	}
		return Stream.empty();
		
	}
	
	
	@SubscribeEvent
	public static void render1stPerson(RenderLevelStageEvent event) {
		if (event.getStage() == CustomLevelRenderStages.AFTER_HAND_RENDER) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.getCameraType().isFirstPerson() && mc.cameraEntity instanceof LivingEntity entity) {
				Stream<BlindingEffect> effects = getEffects(entity);
				effects.forEach(effect -> {
					PoseStack poseStack = new PoseStack();
					poseStack.pushPose();
					ClientUtil.applyCameraTransform(poseStack);
					float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
					renderEffect(poseStack, effect, partialTick);
					poseStack.popPose();
				});
			}
		}
	}

	public static final Random random = new Random();
	public static final float BLOOD_START_DRIPPING = 20;
	public static final float BLOOD_START_DRIPPING_MAX = 40;
	public static final float BLOOD_DRIPPING_ACCEL_TIME = BLOOD_START_DRIPPING_MAX - BLOOD_START_DRIPPING;
	public static final float BLOOD_TIME_FADE_OUT = 10;
	public static void renderEffect(PoseStack poseStack, BlindingEffect effect, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ResourceLocation particleId = UtilFunctions.getId(ModParticles.BLOOD);
		ParticleEngine.MutableSpriteSet spriteSet = mc.particleEngine.spriteSets.get(particleId);
		if (spriteSet != null && !spriteSet.sprites.isEmpty()) {
			random.setSeed(effect.clientSpritesSeed);

			Window window = mc.getWindow();
			float screenWidthRatio = (float) window.getWidth() / (float) window.getHeight();
			int spriteCount = (int) (screenWidthRatio * 100 * effect.ratio);
			float tick = effect.tickCount + partialTick;

			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			Matrix4f matrix4f = poseStack.last().pose();
			RenderSystem.enableBlend();
			float alpha = Mth.clamp((float) (effect.lifeSpan - tick) / effect.lifeSpan * (float) effect.lifeSpan / BLOOD_TIME_FADE_OUT, 0, 1);
			RenderSystem.setShaderColor(1, 1, 1, alpha);
			BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			
			float yOffset = 0;
			if (tick >= BLOOD_START_DRIPPING) {
				float dripTime = (tick - BLOOD_START_DRIPPING);
				float BLOOD_DRIPPING_ACCEL_TIME = BLOOD_START_DRIPPING_MAX - BLOOD_START_DRIPPING;
				float accelTime = Math.min(dripTime, BLOOD_DRIPPING_ACCEL_TIME);
				yOffset = accelTime * accelTime / BLOOD_DRIPPING_ACCEL_TIME * 0.5f
						+ Math.max(dripTime - BLOOD_DRIPPING_ACCEL_TIME, 0);
				yOffset *= 0.002f;
			}
			
			for (int i = 0; i < spriteCount; i++) {
				float timeOffset = (random.nextFloat() - 0.5f) * 4f;
				float particleTime = Math.max(tick - timeOffset - 5, 0);
				TextureAtlasSprite sprite = spriteSet.sprites.get(random.nextInt(spriteSet.sprites.size()));
				
				//float x = (random.nextFloat() - 0.5f) * screenWidthRatio;
				//float y = (random.nextFloat() - 0.5f) - yOffset;
				// went for normal distribution instead of uniform to group the particles towards the center a bit more
				float x = (float) random.nextGaussian() * 0.25f * screenWidthRatio;
				float y = (float) random.nextGaussian() * 0.25f - yOffset;
				
				float u0 = sprite.getU0();
				float u1 = sprite.getU1();
				float v0 = sprite.getV0();
				float v1 = sprite.getV1();
				
				float scale = 0.15f + (random.nextFloat() - 0.5f) * 0.1f;
				if (particleTime < 2) {
					scale *= particleTime / 2;
				}
				float z = -0.5f;
				bufferbuilder.addVertex(matrix4f, x - scale, y - scale, z).setUv(u0, v0);
				bufferbuilder.addVertex(matrix4f, x + scale, y - scale, z).setUv(u1, v0);
				bufferbuilder.addVertex(matrix4f, x + scale, y + scale, z).setUv(u1, v1);
				bufferbuilder.addVertex(matrix4f, x - scale, y + scale, z).setUv(u0, v1);
			}
			BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
			RenderSystem.disableBlend();
			if (alpha != 1) {
				RenderSystem.setShaderColor(1, 1, 1, 1);
			}
		}
	}
	
}
