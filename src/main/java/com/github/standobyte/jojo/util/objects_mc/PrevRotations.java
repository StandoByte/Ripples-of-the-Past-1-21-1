package com.github.standobyte.jojo.util.objects_mc;

import net.minecraft.world.entity.LivingEntity;

public class PrevRotations {
	public float xRot;
	public float yRot;
	public float yHeadRot;
	public float yBodyRot;

	public void rememberAngles(float xRot, float yRot, float yHeadRot, float yBodyRot) {
		this.xRot = xRot;
		this.yRot = yRot;
		this.yHeadRot = yHeadRot;
		this.yBodyRot = yBodyRot;
	}

	public void rememberAngles(LivingEntity entity) {
		rememberAngles(entity.getXRot(), entity.getYRot(), entity.getYHeadRot(), entity.yBodyRot);
	}

}
