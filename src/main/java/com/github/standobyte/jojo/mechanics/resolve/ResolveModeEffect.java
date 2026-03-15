package com.github.standobyte.jojo.mechanics.resolve;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.customobjects.StatusEffectModified;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ResolveModeEffect extends StatusEffectModified {

	public ResolveModeEffect(MobEffectCategory category, int color) {
		super(category, color);
		isUncurable = true;
		disableCreeperLinger = true;
	}

	@Override
	public void onAdded(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {
		super.onAdded(entity, instance, source);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null && standPower.usesResolve()) {
			standPower.resolveHandler.onResolveEffectStart(standPower, entity, instance);
		}
	}

	@Override
	public void onUpdated(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {
		super.onUpdated(entity, instance, source);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null && standPower.usesResolve()) {
			standPower.resolveHandler.onResolveEffectStart(standPower, entity, instance);
		}
	}

	@Override
	public void onRemoved(LivingEntity entity, MobEffectInstance instance) {
		super.onRemoved(entity, instance);
		StandPower standPower = StandPower.get(entity);
		if (standPower != null) {
			standPower.resolveHandler.onResolveEffectEnd(standPower, entity);
		}
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
//		if (!entity.isInvisible()) {
//			entity.level().addParticle(ModParticles.RESOLVE.get(), 
//					entity.getRandomX(2.5D), entity.getY(entity.getRandom().nextDouble() * 1.5), entity.getRandomZ(2.5D), 0, 0, 0);
//		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 3 == 0;
	}

}
