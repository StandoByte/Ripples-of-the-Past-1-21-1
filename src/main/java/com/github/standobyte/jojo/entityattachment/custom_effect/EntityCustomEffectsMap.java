package com.github.standobyte.jojo.entityattachment.custom_effect;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.entityattachment.custom_effect.sync.SyncStandEffectInstanceData;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityCustomEffectsMap<T extends EntityCustomEffect> implements TickingEntityData, SynchronizablePlayerData, INBTSerializable<CompoundTag> {
	protected final Int2ObjectMap<T> effects = new Int2ObjectLinkedOpenHashMap<>();
	protected final EntityCustomEffectsClass effectsClass;
	public final Entity entity;
	
	public EntityCustomEffectsMap(EntityCustomEffectsClass effectsClass, Entity entity) {
		this.effectsClass = effectsClass;
		this.entity = entity;
		if (effectsClass != EntityCustomEffectsClass.STAND_EFFECT) {
			addTicking(entity);
			addSynchronization(entity);
		}
	}
	
	public void addEffect(T instance) {
		putEffectInstance(instance);
		instance.onStart();
		
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(entity, TrEntityCustomEffectsPacket.add(effectsClass, instance, false));
			if (entity instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, TrEntityCustomEffectsPacket.add(effectsClass, instance, true));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T2 extends T> Stream<T2> getEffectsOfType(EntityCustomEffectType<T2> type) {
		return (Stream<T2>) getEffects().stream().filter(e -> e.effectType == type);
	}

	public <T2 extends T> Optional<T2> getEffectOfType(EntityCustomEffectType<T2> type) {
		return getEffectsOfType(type).findFirst();
	}
	
	public <T2 extends T> T2 getOrCreateEffect(EntityCustomEffectType<T2> effectType) {
		Optional<T2> effect = getEffectOfType(effectType);
		if (effect.isPresent()) {
			return effect.get();
		}
		else {
			T2 newEffect = effectType.create(entity.level());
			addEffect(newEffect);
			return newEffect;
		}
	}


	protected void putEffectInstance(T instance) {
		instance.withEntity(entity);
		effects.put(instance.getId(), instance);
	}
	
	void removeEffect(int effectId) {
		removeEffect(getById(effectId));
	}

	public void removeEffect(T instance) {
		if (instance != null) {
			onEffectRemoved(instance, true);
			effects.remove(instance.getId());
		}
	}
	
	public T getById(int id) {
		return effects.get(id);
	}


	@Override
	public void tick() {
		if (effects.isEmpty()) {
			return;
		}

		Level level = entity.level();
		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			T effect = it.next().getValue();
			if (!effect.isStopped()) {
				effect.onTick();
				if (!effect.isStopped() && !level.isClientSide()) {
					SyncStandEffectInstanceData.tickSyncDirtyData(entity, effectsClass, effect);
				}
			}
			if (effect.isStopped()) {
				onEffectRemoved(effect, true);
				it.remove();
			}
		}
	}


	public Collection<T> getEffects() {
		return effects.values();
	}

	@ApiStatus.Internal
	protected void onEffectRemoved(T instance, boolean removeFromAuxiliaryMap) {
		instance.onStop();
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, TrEntityCustomEffectsPacket.remove(effectsClass, instance));
		}
	}


	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		EntityCustomEffectsMap<T> oldEffects = (EntityCustomEffectsMap<T>) this.effectsClass.get(newPlayer, false);
		if (oldEffects != null) {
			cloneEffects(oldEffects);
		}
	}
	
	@ApiStatus.Internal
	public void cloneEffects(EntityCustomEffectsMap<T> oldEffects) {
		this.effects.clear();
		this.effects.putAll(oldEffects.effects);
		this.effects.values().forEach(effect -> effect.withEntity(this.entity));
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
		syncWithTrackingOrUser(entityAsPlayer);
		syncWithUserOnly(entityAsPlayer);
	}

	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		syncWithTrackingOrUser(trackingPlayer);
	}

	@ApiStatus.Internal
	protected void syncWithUserOnly(ServerPlayer user) {
		effects.values().forEach(effect -> {
			effect.syncWithUserOnly(user);
		});
	}

	@ApiStatus.Internal
	protected void syncWithTrackingOrUser(ServerPlayer player) {
		effects.values().forEach(effect -> {
			Entity entity = effect.getEntity();
			PacketDistributor.sendToPlayer(player, TrEntityCustomEffectsPacket.add(
					effectsClass, effect, player == entity));
			SyncStandEffectInstanceData.onStartedTracking(player, entity, 
					effectsClass, effect);
			effect.syncWithTrackingOrUser(player);
		});
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		ListTag effectsList = new ListTag();
		effects.forEach((id, effect) -> {
			if (!effect.isStopped()) {
				effectsList.add(effect.toNBT(registries));
			}
		});
		nbt.put("Effects", effectsList);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		if (nbt.contains("Effects", Tag.TAG_LIST)) {
			Level level = entity.level();
			nbt.getList("Effects", Tag.TAG_COMPOUND).forEach(effectNBT -> {
				T effect = (T) EntityCustomEffect.fromNBT((CompoundTag) effectNBT, registries, level);
				if (effect != null) {
					try {
						putEffectInstance(effect);
						effect.onStart();
					}
					catch (Exception e) {
						JojoMod.getLogger().error("", e);
					}
				}
			});
		}
	}
	
	// event callbacks

	@ApiStatus.Internal
	public void onStandUserDeath(LivingEntity user) {
		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			T effect = it.next().getValue();
			if (effect.removeOnUserDeath) {
				onEffectRemoved(effect, true);
				it.remove();
			}
		}
	}

	@ApiStatus.Internal
	public void onStandUserRemoved(LivingEntity user) {
		for (T effect : effects.values()) {
			onEffectRemoved(effect, false);
		}
		effects.clear();
	}

	@ApiStatus.Internal
	public void onStandUserLogout(ServerPlayer user) {
		boolean isSingleplayer = !user.server.isPublished();
		if (isSingleplayer) return;

		var it = effects.int2ObjectEntrySet().iterator();
		while (it.hasNext()) {
			T effect = it.next().getValue();
			if (effect.removeOnUserLogout) {
				onEffectRemoved(effect, true);
				it.remove();
			}
		}
	}
	

	@EventBusSubscriber(modid = JojoMod.MOD_ID)
	public static class EventHandler {

		@SubscribeEvent
		public static void onPlayerLogout(PlayerLoggedOutEvent event) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			for (EntityCustomEffectsClass type : EntityCustomEffectsClass.values()) {
				var data = type.get(player, false);
				if (data != null) {
					data.onStandUserLogout(player);
				}
			}
		}

		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onLivingDeath(LivingDeathEvent event) {
			LivingEntity dead = event.getEntity();
			if (!dead.level().isClientSide()) {
				for (EntityCustomEffectsClass type : EntityCustomEffectsClass.values()) {
					var data = type.get(dead, false);
					if (data != null) {
						data.onStandUserDeath(dead);
					}
				}
			}
		}
		
		public static void onEntityRemoved(Entity entity) {
			if (entity instanceof LivingEntity living) {
				for (EntityCustomEffectsClass type : EntityCustomEffectsClass.values()) {
					var data = type.get(entity, false);
					if (data != null) {
						data.onStandUserRemoved(living);
					}
				}
			}
		}

	}

}
