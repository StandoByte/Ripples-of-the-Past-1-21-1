package com.github.standobyte.jojo.mechanics.sunburn;

import com.github.standobyte.jojo.customobjects.StatusEffectApplicable;
import com.github.standobyte.jojo.customobjects.StatusEffectModified;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mechanics.JojoDefinitions;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SunBurnStatusEffect extends StatusEffectModified implements StatusEffectApplicable {

	public SunBurnStatusEffect(MobEffectCategory category, int color) {
		super(category, color);
		this.isUncurable = true;
		this.disableCreeperLinger = true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		Level level = entity.level();
		if (level.isClientSide()) {
			double x = entity.getX() + (Math.random() - 0.5) * entity.getBbWidth();
			double y = entity.getY() + Math.random() * entity.getBbHeight();
			double z = entity.getZ() + (Math.random() - 0.5) * entity.getBbWidth();
			if (amplifier < 2) {
				int n = (amplifier + 1) * 2;
				for (int i = 0; i < n; i++) {
					level.addParticle(ParticleTypes.SMOKE, x, y, z, 
							0, 0, 0);
				}
			}
			else {
				int n = (Math.min(amplifier, 5) + 1) / 2;
				for (int i = 0; i < n; i++) {
					level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 
							0, 0, 0);
				}
			}
			if (amplifier >= 4) {
				x = entity.getX() + (Math.random() - 0.5) * entity.getBbWidth();
				y = entity.getY() + Math.random() * entity.getBbHeight();
				z = entity.getZ() + (Math.random() - 0.5) * entity.getBbWidth();
				level.addParticle(ParticleTypes.LAVA, x, y, z, 
						0, 0, 0);
			}
		}
		
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean isApplicable(LivingEntity entity) {
		return JojoDefinitions.isUndeadOrVampiric(entity);
	}



	public static float vampireHealingMultiplier(LivingEntity entity) {
		if (entity.hasEffect(ModStatusEffects.VAMPIRE_SUN_BURN)) {
			return 0;
		}
		
		MobEffectInstance hamonBurn = entity.getEffect(ModStatusEffects.HAMON_SPREAD);
		if (hamonBurn != null) {
			float multiplier = 1 - (float) Math.min(hamonBurn.getAmplifier() + 1, 5) * 0.2F;
			return multiplier;
		}
		
		return 1;
	}

	public static void giveEffectTo(LivingEntity entity, int duration, int amplifier) {
		entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, false, true));
		entity.addEffect(new MobEffectInstance(ModStatusEffects.VAMPIRE_SUN_BURN, duration, amplifier, false, false, false));
	}
}
