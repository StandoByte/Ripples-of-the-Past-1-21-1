package com.github.standobyte.jojo.mixininterface;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface ParticleExtension {
	public void jojo_ripples$tieToEntity(Entity entity);
	public Entity jojo_ripples$getEntityTiedTo();
	public void jojo_ripples$updateTickEntityPos(double x, double y, double z);
	
	public static void onTickPost(Particle particle) {
		ParticleExtension particle_ = (ParticleExtension) particle;
		Entity entity = particle_.jojo_ripples$getEntityTiedTo();
		if (entity != null) {
			if (entity.isAlive()) {
				Vec3 position = entity.position();
				particle_.jojo_ripples$updateTickEntityPos(position.x, position.y, position.z);
			}
			else {
				particle_.jojo_ripples$tieToEntity(null);
			}
		}
	}
	
}
