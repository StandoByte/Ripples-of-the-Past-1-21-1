package com.github.standobyte.jojoimpl.powers.hamon.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CritParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class HamonSparkParticle extends CritParticle {

	public HamonSparkParticle(ClientLevel level, double x, double y, double z,
			double xDDDDD, double yd, double zd) {
		super(level, x, y, z, xDDDDD, yd, zd);
	}

	@Override
	public void tick() {
		super.tick();
		rCol = 1;
		gCol = 1;
		bCol = 1;
	}

	@Override
	protected int getLightColor(float partialTick) {
		return 0xF000F0;
	}


	public static class HamonParticleFactory extends CritParticle.Provider {
		private final SpriteSet sprite;

		public HamonParticleFactory(SpriteSet sprite) {
			super(sprite);
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
			HamonSparkParticle particle = new HamonSparkParticle(level, x, y, z, xd, yd, zd);
			particle.pickSprite(sprite);
			particle.setColor(1, 1, 1);
			return particle;
		}
	}
}
