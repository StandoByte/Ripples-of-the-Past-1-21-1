package com.github.standobyte.jojo.mechanics.sunburn;

import com.github.standobyte.jojo.customobjects.StatusEffectModified;
import com.github.standobyte.jojo.init.ModStatusEffects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HamonSpreadStatusEffect extends StatusEffectModified {

	public HamonSpreadStatusEffect(MobEffectCategory type, int liquidColor) {
		super(type, liquidColor);
		this.isUncurable = true;
		this.disableCreeperLinger = true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
		if (!livingEntity.level().isClientSide() && livingEntity instanceof FlyingMob) {
			double gravity = /* livingEntity.getAttributeValue(Attributes.GRAVITY) */ LivingEntity.DEFAULT_BASE_GRAVITY;
			Vec3 deltaMovement = livingEntity.getDeltaMovement();
			livingEntity.setDeltaMovement(new Vec3(deltaMovement.x, -gravity * (amplifier + 1) * 0.5, deltaMovement.z));
		}
		
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}



	public static void giveEffectTo(LivingEntity entity, int duration, int amplifier) {
		entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, false, true));
		entity.addEffect(new MobEffectInstance(ModStatusEffects.HAMON_SPREAD, duration, amplifier, false, false, false));
	}
}
