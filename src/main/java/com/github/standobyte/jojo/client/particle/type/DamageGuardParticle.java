package com.github.standobyte.jojo.client.particle.type;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DamageGuardParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	protected DamageGuardParticle(ClientLevel level, double x, double y, double z, double dmgAmount, SpriteSet sprites) {
		super(level, x, y, z, 0.0, 0.0, 0.0);
		this.sprites = sprites;
		this.lifetime = 4;
		float brightness = this.random.nextFloat() * 0.6F + 0.4F;
		this.rCol = brightness;
		this.gCol = brightness;
		this.bCol = brightness;
		this.quadSize = Mth.clamp((float) dmgAmount * 0.06667f, 0.25f, 1.0f);
		this.setSpriteFromAge(sprites);
	}

	@Override
	public int getLightColor(float partialTick) {
		return ClientUtil.MAX_LIGHT;
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
	public void render(VertexConsumer buffer, Camera camera, float partialTick) {
		setAlpha(ClientUtil.particleFadeOutAlpha(age, lifetime, partialTick));
		super.render(buffer, camera, partialTick);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet sprites) {
			this.sprites = sprites;
		}

		public Particle createParticle(
				SimpleParticleType type,
				ClientLevel level,
				double x,
				double y,
				double z,
				double xSpeed,
				double ySpeed,
				double zSpeed
				) {
			return new DamageGuardParticle(level, x, y, z, xSpeed, this.sprites);
		}
	}
}
