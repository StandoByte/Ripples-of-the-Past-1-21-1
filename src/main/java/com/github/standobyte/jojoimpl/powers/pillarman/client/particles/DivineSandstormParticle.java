package com.github.standobyte.jojoimpl.powers.pillarman.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DivineSandstormParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected DivineSandstormParticle(ClientLevel level, double pX, double pY, double pZ, double pQuadSizeMulitiplier, SpriteSet pSprites) {
		super(level, pX, pY, pZ, 0.0D, 0.0D, 0.0D);
		this.lifetime = 6 + this.random.nextInt(4);
		float f = this.random.nextFloat() * 0.6F + 0.4F;
		this.rCol = f;
		this.gCol = f;
		this.bCol = f;
		this.quadSize = 2.0F * (1.0F - (float)pQuadSizeMulitiplier * 0.5F);
		this.sprites = pSprites;
		this.setSpriteFromAge(pSprites);
		alpha = 0.2F;
	}

	@Override
	public int getLightColor(float pPartialTick) {
		return 0xF000F0;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(this.sprites);
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
			DivineSandstormParticle sandstormparticle = new DivineSandstormParticle(pLevel, pX, pY, pZ, pXSpeed, this.sprites);
			return sandstormparticle;
		}
	}
}