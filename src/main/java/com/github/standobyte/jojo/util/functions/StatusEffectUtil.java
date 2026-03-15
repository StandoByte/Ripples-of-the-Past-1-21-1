package com.github.standobyte.jojo.util.functions;

import com.github.standobyte.jojo.mixin.statuseffect.LivingEntityMobEffectStuffAccessor;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class StatusEffectUtil {

	public static boolean reduceEffect(LivingEntity entity, Holder<MobEffect> effect, int reduceDuration, int reduceAmplifier) {
		MobEffectInstance mainEffectInstance = entity.getEffect(effect);
		if (mainEffectInstance == null) {
			return false;
		}

		MobEffectInstance effectInstance = mainEffectInstance;
		MobEffectInstance prevInstance = null;

		while (effectInstance != null) {
			int curAmplifier = effectInstance.getAmplifier();
			if (curAmplifier < reduceAmplifier || effectInstance.getDuration() <= reduceDuration) {
				if (effectInstance == mainEffectInstance) {
					return entity.removeEffect(effect);
				}
				else {
					prevInstance.hiddenEffect = null;
					break;
				}
			}

			effectInstance.duration -= reduceDuration;
			if (reduceAmplifier > 0) {
				effectInstance.amplifier -= reduceAmplifier;
			}

			prevInstance = effectInstance;
			effectInstance = effectInstance.hiddenEffect;
		}

		((LivingEntityMobEffectStuffAccessor) entity).invokeOnEffectUpdated(mainEffectInstance, true, null);
		return true;
	}
}
