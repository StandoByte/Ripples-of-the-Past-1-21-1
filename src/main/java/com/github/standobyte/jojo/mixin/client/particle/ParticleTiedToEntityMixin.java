package com.github.standobyte.jojo.mixin.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.github.standobyte.jojo.mixininterface.ParticleExtension;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Particle.class)
public abstract class ParticleTiedToEntityMixin implements ParticleExtension {
	@Shadow protected double xo;
	@Shadow protected double yo;
	@Shadow protected double zo;
	@Shadow protected double x;
	@Shadow protected double y;
	@Shadow protected double z;

	@Shadow public abstract AABB getBoundingBox();
	@Shadow public abstract void setBoundingBox(AABB bb);

	@Unique private Entity entity;
	@Unique private boolean firstEntityTick = true;
	@Unique private double lastEntityTickX;
	@Unique private double lastEntityTickY;
	@Unique private double lastEntityTickZ;

	@Override
	public void jojo_ripples$tieToEntity(Entity entity) {
		this.entity = entity;
		if (entity != null) {
			Vec3 position = entity.position();
			lastEntityTickX = position.x;
			lastEntityTickY = position.y;
			lastEntityTickZ = position.z;
		}
	}

	@Override
	public Entity jojo_ripples$getEntityTiedTo() {
		return entity;
	}

	@Override
	public void jojo_ripples$updateTickEntityPos(double x, double y, double z) {
		double xOffset = x - lastEntityTickX;
		double yOffset = y - lastEntityTickY;
		double zOffset = z - lastEntityTickZ;

		if (xOffset != 0.0 || yOffset != 0.0 || zOffset != 0.0) {
			AABB aabb = this.getBoundingBox().move(xOffset, yOffset, zOffset);
			this.setBoundingBox(aabb);
			this.x += xOffset;
			this.y += yOffset;
			this.z += zOffset;
		}
		
		lastEntityTickX = x;
		lastEntityTickY = y;
		lastEntityTickZ = z;
	}

}
