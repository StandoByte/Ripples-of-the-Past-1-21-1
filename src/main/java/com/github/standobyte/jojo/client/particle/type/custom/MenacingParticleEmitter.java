package com.github.standobyte.jojo.client.particle.type.custom;

import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.client.particle.type.OnomatopoeiaParticle;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TrackingEmitter;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class MenacingParticleEmitter extends TrackingEmitter {
	private final Entity entity;
	private int life;
	private final int lifeTime;
	private final SimpleParticleType particleType;
	private final Vec3 offset1;
	private final Vec3 offset2;
	private boolean superConstructorTicked;
	private ParticleProvider<SimpleParticleType> particleFactory;

	public MenacingParticleEmitter(ClientLevel level, Entity entity, SimpleParticleType particleType, Player player) {
		super(level, entity, particleType, 40);
		this.entity = entity;
		this.lifeTime = 200;
		this.particleType = particleType;

		Vec3 vecFromPlayer = entity.position().subtract(player.position());
		float yRot = (90 - MathUtil.yRotDegFromVec(vecFromPlayer)) * MathUtil.DEG_TO_RAD;
		this.offset1 = new Vec3(0, 0, entity.getBbWidth()).yRot(yRot);
		this.offset2 = new Vec3(0, 0, -(entity.getBbWidth())).yRot(yRot);

		SpriteSet sprites = CustomParticlesHelper.getSavedSpriteSet((ParticleType<?>) particleType);
		if (sprites != null) {
			this.particleFactory = new OnomatopoeiaParticle.GoFactory(sprites);
		}

		this.tick();
	}

	@Override
	public void tick() {
		if (!superConstructorTicked) {
			superConstructorTicked = true;
			return;
		}

		if (life % 40 == 0) {
			addGoParticle(entity.position().add(offset1));
			addGoParticle(entity.position().add(offset2));
		}

		++life;
		if (life >= lifeTime) {
			remove();
		}

	}

	protected void addGoParticle(Vec3 pos) {
		if (particleFactory != null) {
			Particle particle = particleFactory.createParticle(particleType, level, 
					pos.x, pos.y, pos.z, 0, 0.01, 0);
			particle.setLifetime(200);
			Minecraft.getInstance().particleEngine.add(particle);
		}
		else {
			level.addParticle(particleType, false, pos.x, pos.y, pos.z, 0, 0.01, 0);
		}
	}

}
