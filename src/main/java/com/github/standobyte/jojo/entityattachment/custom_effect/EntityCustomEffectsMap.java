package com.github.standobyte.jojo.entityattachment.custom_effect;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.ApiStatus;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityCustomEffectsMap<T extends EntityCustomEffect> implements TickingEntityData, SynchronizablePlayerData, INBTSerializable<CompoundTag> {
	public static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
	protected final Int2ObjectMap<T> effects = new Int2ObjectLinkedOpenHashMap<>();
	protected final EntityCustomEffectsClass effectsClass;
	public final Entity entity;
	
	public EntityCustomEffectsMap(EntityCustomEffectsClass effectsClass, Entity entity) {
		this.effectsClass = effectsClass;
		this.entity = entity;
		addTicking(entity);
		addSynchronization(entity);
	}
	
	protected Entity getEntity() {
		return entity;
	}
	
	public void addEffect(T instance) {
		Entity entity = getEntity();
		if (!entity.level().isClientSide()) {
			instance.withId(EFFECTS_COUNTER.incrementAndGet());
		}
		putEffectInstance(instance);
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(entity, TrEntityCustomEffectsPacket.add(effectsClass, instance, false));
			if (entity instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, TrEntityCustomEffectsPacket.add(effectsClass, instance, true));
			}
		}
	}

	protected void putEffectInstance(T instance) {
		instance.withEntity(getEntity());
		effects.put(instance.getId(), instance);
		instance.onStart();
	}
	
	void removeEffect(int effectId) {
		removeEffect(getById(effectId));
	}

	public void removeEffect(T instance) {
		if (instance != null) {
			onEffectRemoved(instance);
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

		Entity entity = getEntity();
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
				onEffectRemoved(effect);
				it.remove();
			}
		}
	}


	public Collection<T> getEffects() {
		return effects.values();
	}

	@ApiStatus.Internal
	protected void onEffectRemoved(T instance) {
		instance.onStop();
		Entity entity = getEntity();
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, TrEntityCustomEffectsPacket.remove(effectsClass, instance));
		}
	}


	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {}

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
				effectsList.add(effect.toNBT());
			}
		});
		nbt.put("Effects", effectsList);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
		if (nbt.contains("Effects", Tag.TAG_LIST)) {
			Level level = getEntity().level();
			nbt.getList("Effects", Tag.TAG_COMPOUND).forEach(effectNBT -> {
				T effect = (T) EntityCustomEffect.fromNBT((CompoundTag) effectNBT, level);
				if (effect != null) {
					effect.withId(EFFECTS_COUNTER.incrementAndGet());
					putEffectInstance(effect);
				}
			});
		}
	}

}
