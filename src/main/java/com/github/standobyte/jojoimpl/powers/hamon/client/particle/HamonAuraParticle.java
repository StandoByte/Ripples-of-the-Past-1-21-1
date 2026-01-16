package com.github.standobyte.jojoimpl.powers.hamon.client.particle;

import java.util.Random;

import com.github.standobyte.jojo.client.particle.type.RisingParticleOld;
import com.github.standobyte.jojoimpl.powers.hamon.client.particle.custom.HamonAuraParticleRenderType;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class HamonAuraParticle extends RisingParticleOld {
	private static final Random RANDOM = new Random();
	private final int startingSpriteRandom;

	protected HamonAuraParticle(ClientLevel level, double x, double y, double z, 
			double xda, double yda, double zda, SpriteSet sprites) {
		super(level, x, y, z, 0.1F, 0.1F, 0.1F, xda, yda, zda, 1.2F + 0.6F * RANDOM.nextFloat(), sprites, 0.3F, 8, 0.004D, false);
		this.rCol = 1;
		this.gCol = 1;
		this.bCol = 1;
		lifetime = 25 + random.nextInt(10);
		startingSpriteRandom = random.nextInt(lifetime);
		alpha = 0.25F;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return HamonAuraParticleRenderType.HAMON_AURA;
	}

	@Override
	protected int getLightColor(float partialTick) {
		return 0xF000F0;
	}

	private static final float ALPHA_MIN = 0.05F;
	private static final float ALPHA_DIFF = 0.3F;
	@Override
	public void render(VertexConsumer vertexBuilder, Camera camera, float partialTick) {
		float ageF = ((float) age + partialTick) / (float) lifetime;
		float alphaFunc = ageF <= 0.5F ? ageF * 2 : (1 - ageF) * 2;
		this.alpha = ALPHA_MIN + alphaFunc * ALPHA_DIFF;
		super.render(vertexBuilder, camera, partialTick);
	}

	@Override
	public void setSpriteFromAge(SpriteSet pSprite) {
		setSprite(pSprite.get((age + startingSpriteRandom) % lifetime, lifetime));
	}

	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			HamonAuraParticle particle = new HamonAuraParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
			return particle;
		}
	}
}
