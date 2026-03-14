package com.github.standobyte.core_subsystems.entitydata;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.core_subsystems.entitydata.sync.SyncStandEffectInstanceData;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class EntityAttachmentsHolder<T extends TickingEntityAttachment> {
	public static final AtomicInteger EFFECTS_COUNTER = new AtomicInteger();
	protected final Int2ObjectMap<T> effects = new Int2ObjectLinkedOpenHashMap<>();
	protected final EntityAttachmentsClass attachmentsClass;
	
	public EntityAttachmentsHolder(EntityAttachmentsClass attachmentsLocation) {
		this.attachmentsClass = attachmentsLocation;
	}
	
	protected abstract Entity getEntity();
	
	public void addEffect(T instance) {
		Entity entity = getEntity();
		if (!entity.level().isClientSide()) {
			instance.withId(EFFECTS_COUNTER.incrementAndGet());
		}
		putEffectInstance(instance);
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(entity, TrTickingEntityAttachmentPacket.add(attachmentsClass, instance, false));
			if (entity instanceof ServerPlayer player) {
				PacketDistributor.sendToPlayer(player, TrTickingEntityAttachmentPacket.add(attachmentsClass, instance, true));
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


	@ApiStatus.Internal
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
					SyncStandEffectInstanceData.tickSyncDirtyData(entity, attachmentsClass, effect);
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
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, TrTickingEntityAttachmentPacket.remove(attachmentsClass, instance));
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
			Entity entity = effect.getEntity();
			PacketDistributor.sendToPlayer(player, TrTickingEntityAttachmentPacket.add(
					attachmentsClass, effect, player == entity));
			SyncStandEffectInstanceData.onStartedTracking(player, entity, 
					attachmentsClass, effect);
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
			Level level = getEntity().level();
			nbt.getList("Effects", Tag.TAG_COMPOUND).forEach(effectNBT -> {
				T effect = (T) TickingEntityAttachment.fromNBT((CompoundTag) effectNBT, level);
				if (effect != null) {
					effect.withId(EFFECTS_COUNTER.incrementAndGet());
					putEffectInstance(effect);
				}
			});
		}
	}

}
