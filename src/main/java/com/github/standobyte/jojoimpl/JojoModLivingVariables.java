package com.github.standobyte.jojoimpl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class JojoModLivingVariables {
	public static final JojoModLivingVariables DUMMY_INSTANCE = new JojoModLivingVariables();
	
	public boolean isDyingBody = false;
	public Vec3 bleedingParticlesPos = null;
	
	public void tick() {
		bleedingParticlesPos = null;
	}
	
	public static JojoModLivingVariables get(LivingEntity entity) {
		return JojoModLivingVariables.DUMMY_INSTANCE;
	}
}
