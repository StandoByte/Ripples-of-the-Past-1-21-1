package com.github.standobyte.jojo.entityattachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacket;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.network.PacketDistributor;

public class DataEventListeners {
	private final Entity entity;
	
	private Map<Class<?>, SynchronizableEntityData> entityDataSync = new IdentityHashMap<>(12);
	private Map<Class<?>, SynchronizablePlayerData> playerDataSync = new IdentityHashMap<>(12);
	private List<TickingEntityData> _pendingAddToTick = new ArrayList<>(2);
	private Map<Class<?>, TickingEntityData> ticking = new IdentityHashMap<>(12);
	private Map<Class<?>, PostNbtReadEntityData> postNbtCallback = new IdentityHashMap<>(12);

	private List<Pair<String, SynchedDataHelper>> _pendingAddSynchedData = new ArrayList<>(2);
	private Map<String, SynchedDataHelper> synchedDataHolders = new HashMap<>();
	
	public DataEventListeners(IAttachmentHolder entity) {
		this.entity = entity instanceof Entity __ ? __ : null;
	}
	
	
	@ApiStatus.Internal
	public void addEntityDataSync(SynchronizableEntityData data) {
		this.entityDataSync.put(data.getClass(), data);
	}
	
	@ApiStatus.Internal
	public void addPlayerDataSync(SynchronizablePlayerData data) {
		this.entityDataSync.put(data.getClass(), data);
		this.playerDataSync.put(data.getClass(), data);
	}
	
	public void addTickingData(TickingEntityData data) {
		this._pendingAddToTick.add(data);
	}

	public void addSynchedData(String type, SynchedDataHelper synchedData) {
		this._pendingAddSynchedData.add(Pair.of(type, synchedData));
	}

	public void _onStartedTracking(ServerPlayer tracking) {
	}
	
	public void addPostNbtReadCallback(PostNbtReadEntityData data) {
		this.postNbtCallback.put(data.getClass(), data);
	}
	
	
	public void onTracking(ServerPlayer tracking) {
		for (var listener : entityDataSync.values()) {
			listener.syncToTracking(tracking);
		}
		if (entity != null) {
			for (var synchedDataEntry : synchedDataHolders.entrySet()) {
				sendNonDefaultSynched(synchedDataEntry.getValue(), synchedDataEntry.getKey(), entity, tracking);
			}
		}
	}
	
	public void onSyncToPlayer(ServerPlayer player) {
		for (var listener : playerDataSync.values()) {
			listener.syncToPlayer(player);
		}
		for (var synchedDataEntry : synchedDataHolders.entrySet()) {
			sendNonDefaultSynched(synchedDataEntry.getValue(), synchedDataEntry.getKey(), player, player);
		}
	}
	
	private void sendNonDefaultSynched(SynchedDataHelper synchedData, String type, Entity entity, ServerPlayer receiver) {
		List<SynchedEntityData.DataValue<?>> nonDefaultData = synchedData.getDataSyncher().syncOnStartedTracking();
		if (nonDefaultData != null) {
			PacketDistributor.sendToPlayer(receiver, 
					new SynchedDataPacket(entity.getId(), type, nonDefaultData));
		}
	}
	
	public void onClone(Player newPlayer, boolean wasDeath) {
		for (var listener : playerDataSync.values()) {
			listener.onPlayerClone(newPlayer, wasDeath);
		}
	}
	
	public void onTick() {
		if (!_pendingAddToTick.isEmpty()) {
			for (var attachment : _pendingAddToTick) {
				this.ticking.put(attachment.getClass(), attachment);
			}
			_pendingAddToTick.clear();
		}
		for (var listener : ticking.values()) {
			listener.tick();
		}
		
		if (!_pendingAddSynchedData.isEmpty()) {
			for (var entry : _pendingAddSynchedData) {
				this.synchedDataHolders.put(entry.getKey(), entry.getValue());
			}
			_pendingAddSynchedData.clear();
		}
		if (entity != null && !entity.level().isClientSide()) {
			for (var synchedDataEntry : synchedDataHolders.entrySet()) {
				SynchedDataExtended synchedData = synchedDataEntry.getValue().getDataSyncher();
				SynchedDataExtended.tickSyncDirtyData(synchedData, entity);
			}
		}
	}
	
	public void afterNbtRead() {
		for (var listener : postNbtCallback.values()) {
			listener.afterNbtRead();
		}
	}
	
}
