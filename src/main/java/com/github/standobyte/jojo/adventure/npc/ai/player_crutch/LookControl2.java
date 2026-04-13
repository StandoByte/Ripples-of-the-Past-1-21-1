package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import java.util.Optional;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class LookControl2 extends LookControl {
	protected _MobAIVanillaClassesHelper playerAi;
	protected LivingEntity entity;

	public LookControl2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null);
		this.playerAi = playerAi;
		this.entity = entity;
	}

	@Override
	public void setLookAt(double x, double y, double z) {
		this.setLookAt(x, y, z, _MobAIVanillaClassesHelper.getHeadRotSpeed(entity), _MobAIVanillaClassesHelper.getMaxHeadXRot(entity));
	}

	@Override
	public void tick() {
		if (this.resetXRotOnTick()) {
			entity.setXRot(0.0F);
		}

		if (this.lookAtCooldown > 0) {
			this.lookAtCooldown--;
			this.getYRotD().ifPresent(to -> entity.yHeadRot = this.rotateTowards(entity.yHeadRot, to, this.yMaxRotSpeed));
			this.getXRotD().ifPresent(to -> entity.setXRot(this.rotateTowards(entity.getXRot(), to, this.xMaxRotAngle)));
		} else {
			entity.yHeadRot = this.rotateTowards(entity.yHeadRot, entity.yBodyRot, 10.0F);
		}

		this.clampHeadRotationToBody();
	}

	@Override
	protected void clampHeadRotationToBody() {
		PathNavigation pathNavigation = playerAi.navigation;
		if (!pathNavigation.isDone()) {
			entity.yHeadRot = Mth.rotateIfNecessary(entity.yHeadRot, entity.yBodyRot, _MobAIVanillaClassesHelper.getMaxHeadYRot(entity));
		}
	}

	@Override
	protected Optional<Float> getXRotD() {
		double d0 = this.wantedX - entity.getX();
		double d1 = this.wantedY - entity.getEyeY();
		double d2 = this.wantedZ - entity.getZ();
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		return !(Math.abs(d1) > 1.0E-5F) && !(Math.abs(d3) > 1.0E-5F) ? Optional.empty() : Optional.of((float)(-(Mth.atan2(d1, d3) * 180.0F / Math.PI)));
	}

	@Override
	protected Optional<Float> getYRotD() {
		double d0 = this.wantedX - entity.getX();
		double d1 = this.wantedZ - entity.getZ();
		return !(Math.abs(d1) > 1.0E-5F) && !(Math.abs(d0) > 1.0E-5F)
				? Optional.empty()
				: Optional.of((float)(Mth.atan2(d1, d0) * 180.0F / (float)Math.PI) - 90.0F);
	}

}
