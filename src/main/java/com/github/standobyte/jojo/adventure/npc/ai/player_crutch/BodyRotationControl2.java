package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class BodyRotationControl2 extends BodyRotationControl {
	protected LivingEntity entity;
	protected static final int HEAD_STABLE_ANGLE = 15;
	protected static final int DELAY_UNTIL_STARTING_TO_FACE_FORWARD = 10;
	protected static final int HOW_LONG_IT_TAKES_TO_FACE_FORWARD = 10;
	protected int headStableTime;
	protected float lastStableYHeadRot;

	public BodyRotationControl2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null);
		this.entity = entity;
	}

	@Override
	public void clientTick() {
		if (this.isMoving()) {
			entity.yBodyRot = entity.getYRot();
			this.rotateHeadIfNecessary();
			this.lastStableYHeadRot = entity.yHeadRot;
			this.headStableTime = 0;
		} else {
			if (this.notCarryingMobPassengers()) {
				if (Math.abs(entity.yHeadRot - this.lastStableYHeadRot) > 15.0F) {
					this.headStableTime = 0;
					this.lastStableYHeadRot = entity.yHeadRot;
					this.rotateBodyIfNecessary();
				} else {
					this.headStableTime++;
					if (this.headStableTime > 10) {
						this.rotateHeadTowardsFront();
					}
				}
			}
		}
	}

	protected void rotateBodyIfNecessary() {
		entity.yBodyRot = Mth.rotateIfNecessary(entity.yBodyRot, entity.yHeadRot, _MobAIVanillaClassesHelper.getMaxHeadYRot(entity));
	}

	protected void rotateHeadIfNecessary() {
		entity.yHeadRot = Mth.rotateIfNecessary(entity.yHeadRot, entity.yBodyRot, _MobAIVanillaClassesHelper.getMaxHeadYRot(entity));
	}

	protected void rotateHeadTowardsFront() {
		int i = this.headStableTime - 10;
		float f = Mth.clamp((float)i / 10.0F, 0.0F, 1.0F);
		float f1 = _MobAIVanillaClassesHelper.getMaxHeadYRot(entity) * (1.0F - f);
		entity.yBodyRot = Mth.rotateIfNecessary(entity.yBodyRot, entity.yHeadRot, f1);
	}

	protected boolean notCarryingMobPassengers() {
		return !(entity.getFirstPassenger() instanceof Mob);
	}

	protected boolean isMoving() {
		double d0 = entity.getX() - entity.xo;
		double d1 = entity.getZ() - entity.zo;
		return d0 * d0 + d1 * d1 > 2.5000003E-7F;
	}

}
