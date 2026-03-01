package com.github.standobyte.jojoimpl.stands.crazydiamond.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class CrazyDRestorationParticle extends TextureSheetParticle {

	protected CrazyDRestorationParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private static Factory instance;
		private final SpriteSet sprite;

		public Factory(SpriteSet sprite) {
			this.sprite = sprite;
			instance = this;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			CrazyDRestorationParticle particle = new CrazyDRestorationParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(sprite);
			return particle;
		}

		public static SpriteSet getSprite() {
			return instance.sprite;
		}
	}
}
