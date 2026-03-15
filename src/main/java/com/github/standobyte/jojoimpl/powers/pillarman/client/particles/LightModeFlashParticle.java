package com.github.standobyte.jojoimpl.powers.pillarman.client.particles;

import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class LightModeFlashParticle extends TextureSheetParticle {

	private LightModeFlashParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		this.lifetime = 4;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
//		return HamonAuraParticleRenderType.HAMON_AURA; // KEKW
	}

	@Override
	protected int getLightColor(float partialTick) {
		return ClientUtil.MAX_LIGHT;
	}

	@Override
	public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
		this.setAlpha(0.6F - ((float)this.age + pPartialTicks - 1.0F) * 0.25F * 0.5F);
		super.render(pBuffer, pRenderInfo, pPartialTicks);
	}

	@Override
	public float getQuadSize(float pScaleFactor) {
		return 7.1F * Mth.sin(((float)this.age + pScaleFactor - 1.0F) * 0.25F * (float)Math.PI);
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			LightModeFlashParticle particle = new LightModeFlashParticle(level, x, y, z);
			particle.pickSprite(spriteSet);
			return particle;
		}
	}

}
