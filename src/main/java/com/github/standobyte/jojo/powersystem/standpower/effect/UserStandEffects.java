package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.core_subsystems.entitydata.EntityCustomEffectsClass;
import com.github.standobyte.core_subsystems.entitydata.EntityCustomEffectType;
import com.github.standobyte.core_subsystems.entitydata.EntityCustomEffectsMap;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.server.level.ServerPlayer;
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


	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> Optional<T> getEffectTargeting(EntityCustomEffectType<T> effectType, LivingEntity target) {
		Stream<StandEffectInstance> effects = getEffects().stream().filter(effect -> 
				effect.effectType == effectType && 
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
			T newEffect = effectType.create(getEntity().level());
			addEffect(newEffect.withTarget(target));
			return newEffect;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> T getOrCreateEffect(EntityCustomEffectType<T> effectType) {
		Optional<T> effect = (Optional<T>) getEffects().stream()
				.filter(e -> e.effectType == effectType)
				.findFirst();
		if (effect.isPresent()) {
			return effect.get();
		}
		else {
			T newEffect = effectType.create(getEntity().level());
			addEffect(newEffect);
			return newEffect;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> Stream<T> getEffectsOfType(EntityCustomEffectType<T> type) {
		return (Stream<T>) getEffects().stream()
				.filter(effect -> effect.effectType == type);
	}

	public <T extends StandEffectInstance> Optional<T> getEffectOfType(EntityCustomEffectType<T> type) {
		return getEffectsOfType(type).findFirst();
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

	@SuppressWarnings("unchecked")
	public static <T extends StandEffectInstance> Stream<T> getEffectsTargetedBy(LivingEntity entity, EntityCustomEffectType<T> type) {
		return (Stream<T>) StandEffectsTarget.getEffectsReadOnly(entity).filter(effect -> effect.effectType == type);
	}

	public static boolean isTargetedBy(LivingEntity entity, EntityCustomEffectType<? extends StandEffectInstance> type) {
		return getEffectsTargetedBy(entity, type).findAny().isPresent();
	}


	@ApiStatus.Internal
	public void setPowerData(StandPower standPower) {
		effects.values().forEach(effect -> effect.withStand(standPower));
	}

	@ApiStatus.Internal
	public void onStandUserDeath(LivingEntity user) {
		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			StandEffectInstance effect = it.next().getValue();
			if (effect.removeOnUserDeath) {
				onEffectRemoved(effect);
				it.remove();
			}
		}
	}

	@ApiStatus.Internal
	public void onStandUserRemoved(LivingEntity user) {
		for (StandEffectInstance effect : effects.values()) {
			onEffectRemoved(effect);
		}
	}

	@ApiStatus.Internal
	public void onStandUserLogout(ServerPlayer user) {
		if (!user.server.isPublished()) return;

		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			StandEffectInstance effect = it.next().getValue();
			if (effect.removeOnUserLogout) {
				onEffectRemoved(effect);
				it.remove();
			}
		}
	}

	@ApiStatus.Internal
	public void onStandChanged(LivingEntity user) {
		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			StandEffectInstance effect = it.next().getValue();
			if (effect.removeOnStandChanged) {
				onEffectRemoved(effect);
				it.remove();
			}
		}
	}

}
