package com.github.standobyte.jojo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class LivingEntityVariables {
	public static final LivingEntityVariables DUMMY_INSTANCE = new LivingEntityVariables();
	
	public boolean isDyingBody = false;
	public Vec3 bleedingParticlesPos = null;
	
	public void tick() {
		bleedingParticlesPos = null;
	}
	
	public static LivingEntityVariables get(LivingEntity entity) {
		return LivingEntityVariables.DUMMY_INSTANCE;
	}
}
