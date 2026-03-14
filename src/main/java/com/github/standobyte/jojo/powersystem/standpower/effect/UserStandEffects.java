package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.core_subsystems.entitydata.EntityAttachmentType;
import com.github.standobyte.core_subsystems.entitydata.TickingEntityAttachment;
import com.github.standobyte.core_subsystems.entitydata.TrTickingEntityAttachmentPacket;
import com.github.standobyte.core_subsystems.entitydata.TrTickingEntityAttachmentPacket.AttachmentType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class UserStandEffects {
	public static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
	protected StandPower standPower;
	protected final Int2ObjectMap<StandEffectInstance> effects = new Int2ObjectLinkedOpenHashMap<>();

	public UserStandEffects(StandPower standPower) {
		this.standPower = standPower;
	}

	public void addEffect(StandEffectInstance instance) {
		LivingEntity user = standPower.getUser();
		if (!user.level().isClientSide()) {
			instance.withId(EFFECTS_COUNTER.incrementAndGet());
		}
		putEffectInstance(instance);
		if (!user.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, TrTickingEntityAttachmentPacket.add(AttachmentType.STAND_EFFECT, instance, false));
			if (user instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, TrTickingEntityAttachmentPacket.add(AttachmentType.STAND_EFFECT, instance, true));
			}
		}
	}

	protected void putEffectInstance(StandEffectInstance instance) {
		instance.withStand(standPower);
		effects.put(instance.getId(), instance);
		instance.onStart();
	}

	public void removeEffect(StandEffectInstance instance) {
		if (instance != null) {
			onEffectRemoved(instance);
			effects.remove(instance.getId());
		}
	}

//	public void onUserStandRemoved(LivingEntity user) {
//		effects.values().forEach(effect -> effect.onStop());
//		effects.clear();
//		if (!user.level.isClientSide()) {
//			PacketManager.sendToClientsTrackingAndSelf(TrStandEffectPacket.removeAll(user), user);
//		}
//	}

	public StandEffectInstance getById(int id) {
		return effects.get(id);
	}


	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> Optional<T> getEffectTargeting(EntityAttachmentType<T> effectType, LivingEntity target) {
		Stream<StandEffectInstance> effects = getEffects().stream().filter(effect -> 
				effect.effectType == effectType && 
				(target == null ? effect.getTargetUUID() == null : target.getUUID().equals(effect.getTargetUUID())));
		Optional<T> effect = (Optional<T>) effects.findFirst();
		return effect;
	}

	public <T extends StandEffectInstance> T getOrCreateEffect(EntityAttachmentType<T> effectType, LivingEntity target) {
		Optional<T> effect = getEffectTargeting(effectType, target);
		if (effect.isPresent()) {
			return effect.get();
		}
		else {
			T newEffect = effectType.create(standPower.getUser().level());
			addEffect(newEffect.withTarget(target));
			return newEffect;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> T getOrCreateEffect(EntityAttachmentType<T> effectType) {
		Optional<T> effect = (Optional<T>) getEffects().stream()
				.filter(e -> e.effectType == effectType)
				.findFirst();
		if (effect.isPresent()) {
			return effect.get();
		}
		else {
			T newEffect = effectType.create(standPower.getUser().level());
			addEffect(newEffect);
			return newEffect;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends StandEffectInstance> Stream<T> getEffectsOfType(EntityAttachmentType<T> type) {
		return (Stream<T>) getEffects().stream()
				.filter(effect -> effect.effectType == type);
	}

	public <T extends StandEffectInstance> Optional<T> getEffectOfType(EntityAttachmentType<T> type) {
		return getEffectsOfType(type).findFirst();
	}


	public static <T extends StandEffectInstance> Stream<T> getEffectsOfType(LivingEntity user, EntityAttachmentType<T> type) {
		StandPower power = StandPower.get(user);
		return power != null ? power.userStandEffects.getEffectsOfType(type) : null;
	}

	public static <T extends StandEffectInstance> Optional<T> getEffectOfType(LivingEntity user, EntityAttachmentType<T> type) {
		StandPower power = StandPower.get(user);
		return power != null ? power.userStandEffects.getEffectOfType(type) : null;
	}
	
	public static <T extends StandEffectInstance> Stream<T> getEffectsInRange(StandPower power, EntityAttachmentType<T> type, double range, LivingEntity user) {
		double rangeSqr = range * range;
		return power.userStandEffects.getEffectsOfType(type)
				.filter(effect -> {
					Entity target = effect.getTarget();
					return target != null && target.distanceToSqr(user) < rangeSqr;
				});
	}

	@SuppressWarnings("unchecked")
	public static <T extends StandEffectInstance> Optional<T> getEffectLookedAt(StandPower power, EntityAttachmentType<T> type, double range, LivingEntity user) {
		return (Optional<T>) getTargetLookedAt(getEffectsInRange(power, type, range, user), user);
	}

	public static Optional<StandEffectInstance> getTargetLookedAt(Stream<? extends StandEffectInstance> targets, LivingEntity user) {
		Vec3 lookAngle = user.getLookAngle();
		Vec3 eyePos = user.getEyePosition(1.0F);
		return targets.max(Comparator.comparingDouble(
				e -> lookAngle.dot(e.getTarget().getBoundingBox().getCenter().subtract(eyePos).normalize())))
				.map(Function.identity());
	}

	public Collection<StandEffectInstance> getEffects() {
		return effects.values();
	}

	@SuppressWarnings("unchecked")
	public static <T extends StandEffectInstance> Stream<T> getEffectsTargetedBy(LivingEntity entity, EntityAttachmentType<T> type) {
		return (Stream<T>) StandEffectsTarget.getEffectsReadOnly(entity).filter(effect -> effect.effectType == type);
	}

	public static boolean isTargetedBy(LivingEntity entity, EntityAttachmentType<? extends StandEffectInstance> type) {
		return getEffectsTargetedBy(entity, type).findAny().isPresent();
	}


	@ApiStatus.Internal
	public void setPowerData(StandPower standPower) {
		this.standPower = standPower;
		effects.values().forEach(effect -> effect.withStand(standPower));
	}

	@ApiStatus.Internal
	public void tick() {
		if (effects.isEmpty()) {
			return;
		}

		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			StandEffectInstance effect = it.next().getValue();
			if (!effect.isStopped()) {
				effect.onTick();
			}
			if (effect.isStopped()) {
				onEffectRemoved(effect);
				it.remove();
			}
		}
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

	@ApiStatus.Internal
	protected void onEffectRemoved(StandEffectInstance instance) {
		instance.onStop();
		LivingEntity user = standPower.getUser();
		if (!user.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, TrTickingEntityAttachmentPacket.remove(AttachmentType.STAND_EFFECT, instance));
		}
	}


	@ApiStatus.Internal
	public void syncWithUserOnly(ServerPlayer user) {
		effects.values().forEach(effect -> {
			effect.syncWithUserOnly(user);
		});
	}

	@ApiStatus.Internal
	public void syncWithTrackingOrUser(ServerPlayer player) {
		effects.values().forEach(effect -> {
			PacketDistributor.sendToPlayer(player, TrTickingEntityAttachmentPacket.add(
					AttachmentType.STAND_EFFECT, effect, player == effect.getStandUser()));
			effect.syncWithTrackingOrUser(player);
		});
	}

	@ApiStatus.Internal
	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		ListTag effectsList = new ListTag();
		effects.forEach((id, effect) -> {
			if (!effect.isStopped()) {
				effectsList.add(effect.toNBT());
			}
		});
		nbt.put("Effects", effectsList);
		return nbt;
	}

	@ApiStatus.Internal
	public void readNBT(CompoundTag nbt) {
		if (nbt.contains("Effects", Tag.TAG_LIST)) {
			Level level = standPower.getUser().level();
			nbt.getList("Effects", Tag.TAG_COMPOUND).forEach(effectNBT -> {
				StandEffectInstance effect = (StandEffectInstance) TickingEntityAttachment.fromNBT((CompoundTag) effectNBT, level);
				if (effect != null) {
					effect.withId(EFFECTS_COUNTER.incrementAndGet());
					putEffectInstance(effect);
				}
			});
		}
	}

}
