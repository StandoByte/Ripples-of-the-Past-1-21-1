package com.github.standobyte.jojo.client.particle.type;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class OnomatopoeiaParticle extends TextureSheetParticle {
	private double offsetX;
	private double offsetY;
	private double offsetZ;

	protected OnomatopoeiaParticle(ClientLevel level, double posX, double posY, double posZ) {
		this(level, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
	}

	public OnomatopoeiaParticle(SimpleParticleType type, ClientLevel level, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
		this(level, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
	}

	protected OnomatopoeiaParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed) {
		super(level, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
		quadSize = 0.12F + random.nextFloat() * 0.06F;
		hasPhysics = false;
		xd = xSpeed;
		yd = ySpeed;
		zd = zSpeed;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		xo = x;
		yo = y;
		zo = z;
		if (age++ >= lifetime) {
			remove();
		}
		else {
			double x = xd - offsetX;
			double y = yd - offsetY;
			double z = zd - offsetZ;
			offsetX = (Math.random() - 0.5) * 0.02;
			offsetY = (Math.random() - 0.5) * 0.01;
			offsetZ = (Math.random() - 0.5) * 0.02;
			move(x + offsetX, y + offsetY, z + offsetZ);
		}
		alpha = Mth.clamp((float) lifetime / (float) age * 3F - 3F, 0F, 1F);
	}

	public static class GoFactory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public GoFactory(SpriteSet sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			OnomatopoeiaParticle particle = new OnomatopoeiaParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(spriteSet);
			particle.setLifetime(40);
			return particle;
		}
	}

	public static class DoFactory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public DoFactory(SpriteSet sprite) {
			this.spriteSet = sprite;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			OnomatopoeiaParticle particle = new OnomatopoeiaParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(spriteSet);
			particle.setLifetime(40);
			particle.scale(2F);
			return particle;
		}
	}
}