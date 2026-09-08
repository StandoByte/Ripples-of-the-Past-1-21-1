package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsClass;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsMap;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class UserStandEffects extends EntityCustomEffectsMap<StandEffectInstance> {

	public UserStandEffects(StandPower standPower) {
		super(EntityCustomEffectsClass.STAND_EFFECT, standPower.getUser());
	}
	
//	public void onUserStandRemoved(LivingEntity user) {
//		effects.values().forEach(effect -> effect.onStop());
//		effects.clear();
//		if (!user.level.isClientSide()) {
//			PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.removeAll(user), user);
//		}
//	}

	public <T extends StandEffectInstance> Optional<T> getEffectTargeting(EntityCustomEffectType<T> effectType, LivingEntity target) {
		Stream<T> effects = getEffectsOfType(effectType).filter(effect -> 
				(target == null ? effect.getTargetUUID() == null : target.getUUID().equals(effect.getTargetUUID())));
		Optional<T> effect = (Optional<T>) effects.findFirst();
		return effect;
	}

	public <T extends StandEffectInstance> T getOrCreateEffect(EntityCustomEffectType<T> effectType, LivingEntity target) {
		Optional<T> effect = getEffectTargeting(effectType, target);
		if (effect.isPresent()) {
			return effect.get();
		}
		else {
			T newEffect = effectType.create(entity.level());
			addEffect(newEffect.withTarget(target));
			return newEffect;
		}
	}

	public static <T extends StandEffectInstance> Stream<T> getEffectsOfType(LivingEntity user, EntityCustomEffectType<T> type) {
		StandPower power = StandPower.get(user);
		return power != null ? power.userStandEffects.getEffectsOfType(type) : null;
	}

	public static <T extends StandEffectInstance> Optional<T> getEffectOfType(LivingEntity user, EntityCustomEffectType<T> type) {
		StandPower power = StandPower.get(user);
		return power != null ? power.userStandEffects.getEffectOfType(type) : null;
	}
	
	public static <T extends StandEffectInstance> Stream<T> getEffectsInRange(StandPower power, EntityCustomEffectType<T> type, double range, LivingEntity user) {
		double rangeSqr = range * range;
		return power.userStandEffects.getEffectsOfType(type)
				.filter(effect -> {
					Entity target = effect.getTarget();
					return target != null && target.distanceToSqr(user) < rangeSqr;
				});
	}

	@SuppressWarnings("unchecked")
	public static <T extends StandEffectInstance> Optional<T> getEffectLookedAt(StandPower power, EntityCustomEffectType<T> type, double range, LivingEntity user) {
		return (Optional<T>) getTargetLookedAt(getEffectsInRange(power, type, range, user), user);
	}

	public static Optional<StandEffectInstance> getTargetLookedAt(Stream<? extends StandEffectInstance> targets, LivingEntity user) {
		Vec3 lookAngle = user.getLookAngle();
		Vec3 eyePos = user.getEyePosition(1.0F);
		return targets.max(Comparator.comparingDouble(
				e -> lookAngle.dot(e.getTarget().getBoundingBox().getCenter().subtract(eyePos).normalize())))
				.map(Function.identity());
	}

	@Deprecated
	public static <T extends StandEffectInstance> Stream<T> getEffectsTargetedBy(LivingEntity targetEntity, EntityCustomEffectType<T> type) {
		return StandEffectsTarget.getEffectsTargetedBy(targetEntity, type);
	}

	@Deprecated
	public static boolean isTargetedBy(LivingEntity entity, EntityCustomEffectType<? extends StandEffectInstance> type) {
		return StandEffectsTarget.isTargetedBy(entity, type);
	}


	@ApiStatus.Internal
	public void onStandChanged(LivingEntity user) {
		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			StandEffectInstance effect = it.next().getValue();
			if (effect.removeOnStandChanged) {
				onEffectRemoved(effect, true);
				it.remove();
			}
		}
	}

}
