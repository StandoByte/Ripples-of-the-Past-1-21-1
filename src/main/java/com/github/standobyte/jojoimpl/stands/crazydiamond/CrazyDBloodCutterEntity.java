package com.github.standobyte.jojoimpl.stands.crazydiamond;

import java.util.OptionalInt;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.customobjects.entity_projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.mechanics.BleedingEffect;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class CrazyDBloodCutterEntity extends ModdedProjectileEntity {

	public CrazyDBloodCutterEntity(LivingEntity shooter, Level level) {
		super(ModEntityTypes.CD_BLOOD_CUTTER.get(), shooter, level);
	}

	public CrazyDBloodCutterEntity(EntityType<? extends CrazyDBloodCutterEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public int ticksLifespan() {
		return 100;
	}

	@Override
	protected float getBaseDamage() {
		return 4.0F;
	}

	protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
		if (targetType != TargetType.ENTITY || ((EntityHitResult) hitTarget).getEntity() instanceof LivingEntity) {
			super.breakProjectile(targetType, hitTarget);
			splashBlood();
		}
	}

	private void splashBlood() {
		if (isInWaterOrBubble()) return;
		Level level = level();
		if (!level.isClientSide()) {
			BleedingEffect.splashBlood(level, getBoundingBox().getCenter(), 4, 6.4F, OptionalInt.empty(), getOwner());
			level.playSound(null, getX(), getY(), getZ(), ModSoundEvents.WATER_SPLASH.get(), getSoundSource(), 1.0F, 1.0F);
		}
	}

	@Override
	protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
		if (target instanceof LivingEntity targetLiving) {
			PlayerPower targetPower = PlayerPower.get(targetLiving);
			if (targetPower != null && targetPower.getPowerType() == ModPlayerPowers.VAMPIRISM) {
				target.playSound(ModSoundEvents.VAMPIRE_BLOOD_DRAIN.get(), 1.0F, 1.0F);
				targetPower.addEnergy(5F);
				discard();
				return false;
			}
		}
		return super.hurtTarget(target, owner);
	}

	public static boolean canHaveBloodDropsOn(Entity target, StandPower bleedingEntityStand) {
		return !target.is(bleedingEntityStand.getUser()) && target != bleedingEntityStand.getSummonedStand()
				&& !(target instanceof StandEntity stand && !stand.isVisibleForAll())
				&& !target.isInWaterOrBubble();
	}

	@Override
	protected float getMaxHardnessBreakable() {
		return 0;
	}

	@Override
	public boolean standDamage() {
		return false;
	}

}
