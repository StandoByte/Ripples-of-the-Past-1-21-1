package com.github.standobyte.jojo.mechanics.blinding;

import java.util.Random;
import java.util.stream.Stream;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.CustomLevelRenderStages;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsClass;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsMap;
import com.github.standobyte.jojo.init.ModEntityCustomEffects;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.mechanics.blinding.BlindingEffect.ParticlesRNGCache;
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
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class BlindingParticlesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

	public BlindingParticlesLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	public static final Vec3 HEAD_EYES_POS = new Vec3(0, 0.45, 0.6);
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
	public static final float BLINDING_FADE_OUT = 10;
	
	public static final float BLOOD_START_DRIPPING = 20;
	public static final float BLOOD_START_DRIPPING_MAX = 40;
	public static final float BLOOD_DRIPPING_ACCEL_TIME = BLOOD_START_DRIPPING_MAX - BLOOD_START_DRIPPING;
	public static void renderEffect(PoseStack poseStack, BlindingEffect effect, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		Holder<ParticleType<?>> particleType;
		int tint = BlitFloat.NO_TINT;
		
		if (effect.type == null) effect.type = BlindingEffect.BlindingParticlesType.DEFAULT;
		switch (effect.type) {
			case BLOOD -> {
				particleType = ModParticles.BLOOD;
			}
			case SAND -> {
				particleType = BuiltInRegistries.PARTICLE_TYPE.wrapAsHolder(ParticleTypes.FALLING_DUST);
				if (effect.block == null) {
					effect.block = Blocks.SAND.defaultBlockState();
				}
				if (effect.blockPos == null) {
					effect.blockPos = mc.player.blockPosition();
				}
				if (effect.blockTint == -1) {
					effect.blockTint = effect.block.getBlock() instanceof FallingBlock fallingBlock ? 
							fallingBlock.getDustColor(effect.block, mc.level, effect.blockPos) : 
							mc.getBlockColors().getColor(effect.block, mc.level, effect.blockPos);
					effect.blockTint |= 0xff000000;
				}
				tint = effect.blockTint;
			}
			default -> throw new AssertionError();
		}

		ResourceLocation particleId = UtilFunctions.getId(particleType);
		if (particleId == null) return;
		ParticleEngine.MutableSpriteSet spriteSet = mc.particleEngine.spriteSets.get(particleId);
		if (spriteSet == null || spriteSet.sprites.isEmpty()) return;
		
		
		Window window = mc.getWindow();
		float screenWidthRatio = (float) window.getWidth() / (float) window.getHeight();
		int spriteCount = (int) (screenWidthRatio * 100 * effect.ratio);
		float tick = effect.tickCount + partialTick;
		
		float dripDownOffset = 0;
		boolean dripsDown = effect.type == BlindingEffect.BlindingParticlesType.BLOOD;
		if (dripsDown && tick >= BLOOD_START_DRIPPING) {
			float dripTime = (tick - BLOOD_START_DRIPPING);
			float BLOOD_DRIPPING_ACCEL_TIME = BLOOD_START_DRIPPING_MAX - BLOOD_START_DRIPPING;
			float accelTime = Math.min(dripTime, BLOOD_DRIPPING_ACCEL_TIME);
			dripDownOffset = accelTime * accelTime / BLOOD_DRIPPING_ACCEL_TIME * 0.5f
					+ Math.max(dripTime - BLOOD_DRIPPING_ACCEL_TIME, 0);
			//dripDownOffset *= 0.002f;
		}
		
		ParticlesRNGCache sprites = effect.clientSprites;
		if (sprites == null || sprites.spriteCount != spriteCount) {
			random.setSeed(effect.clientSpritesSeed);
			
			float[] timeOffset = new float[spriteCount];
			int[] spriteIndex = new int[spriteCount];
			float[] dripDownSpeed = new float[spriteCount];
			float[] x = new float[spriteCount];
			float[] y = new float[spriteCount];
			float[] scaleMult = new float[spriteCount];
			float[] tintMult = new float[spriteCount];
			
			for (int i = 0; i < spriteCount; i++) {
				timeOffset[i] = (random.nextFloat() - 0.5f) * 4f + 5;
				spriteIndex[i] = random.nextInt(spriteSet.sprites.size());
				dripDownSpeed[i] = dripsDown ? 0.001f + random.nextFloat() * 0.002f : 0;
				
				//x[i] = (random.nextFloat() - 0.5f) * screenWidthRatio;
				//y[i] = (random.nextFloat() - 0.5f) - yOffset;
				// went for normal distribution instead of uniform to group the particles towards the center a bit more
				x[i] = (float) random.nextGaussian() * 0.25f * screenWidthRatio;
				y[i] = (float) random.nextGaussian() * 0.25f;
				scaleMult[i] = (random.nextFloat() - 0.5f) * 0.1f;
				tintMult[i] = random.nextFloat() * 0.15f + 0.85f;
			}
			
			sprites = new ParticlesRNGCache(spriteCount, timeOffset, spriteIndex, dripDownSpeed, x, y, scaleMult, tintMult);
			effect.clientSprites = sprites;
		}

		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
		Matrix4f matrix4f = poseStack.last().pose();
		RenderSystem.enableBlend();
		float fadeOut = Mth.clamp((float) (effect.lifeSpan - tick) / effect.lifeSpan * (float) effect.lifeSpan / BLINDING_FADE_OUT, 0, 1);
		float alpha = fadeOut;
		if (alpha != 1f) {
			RenderSystem.setShaderColor(1, 1, 1, alpha);
		}
		BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
		RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
		
		int light = mc.getEntityRenderDispatcher().getPackedLightCoords(mc.cameraEntity, partialTick);
		for (int i = 0; i < spriteCount; i++) {
			float timeOffset = sprites.timeOffset[i];
			int spriteIndex = sprites.spriteIndex[i];
			float dripDownSpeed = sprites.dripDownSpeed[i];
			float x = sprites.x[i];
			float y = sprites.y[i] - dripDownOffset * dripDownSpeed;
			float scaleMult = sprites.scaleMult[i];
			float tintMult = sprites.tintMult[i];
			
			float particleTime = Math.max(tick - timeOffset, 0);
			TextureAtlasSprite sprite = spriteSet.sprites.get(spriteIndex);
			
			float u0 = sprite.getU0();
			float u1 = sprite.getU1();
			float v0 = sprite.getV0();
			float v1 = sprite.getV1();
			
			float scale = 0.15f + scaleMult;
			if (particleTime < 2) {
				scale *= particleTime / 2;
			}
			
			int particleTint = FastColor.ARGB32.multiply(tint, FastColor.ARGB32.colorFromFloat(1, tintMult, tintMult, tintMult));
			
			float z = -0.5f;
			bufferbuilder.addVertex(matrix4f, x - scale, y - scale, z).setColor(particleTint).setUv(u0, v0).setLight(light) /*doesn't seem to work*/;
			bufferbuilder.addVertex(matrix4f, x + scale, y - scale, z).setColor(particleTint).setUv(u1, v0).setLight(light);
			bufferbuilder.addVertex(matrix4f, x + scale, y + scale, z).setColor(particleTint).setUv(u1, v1).setLight(light);
			bufferbuilder.addVertex(matrix4f, x - scale, y + scale, z).setColor(particleTint).setUv(u0, v1).setLight(light);
		}
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
		RenderSystem.disableBlend();
		if (alpha != 1f) {
			RenderSystem.setShaderColor(1, 1, 1, 1);
		}
	}
	
}
