package com.github.standobyte.jojo.entityattachment;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class DataEventListeners {
	private Map<Class<?>, SynchronizableEntityData> entityDataSync = new IdentityHashMap<>(12);
	private Map<Class<?>, SynchronizablePlayerData> playerDataSync = new IdentityHashMap<>(12);
	private List<TickingEntityData> pendingAddToTick = new ArrayList<>(2);
	private Map<Class<?>, TickingEntityData> ticking = new IdentityHashMap<>(12);
	private Map<Class<?>, PostNbtReadEntityData> postNbtCallback = new IdentityHashMap<>(12);
	
	public DataEventListeners(IAttachmentHolder entity) {}
	
	
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
		this.pendingAddToTick.add(data);
	}
	
	public void addPostNbtReadCallback(PostNbtReadEntityData data) {
		this.postNbtCallback.put(data.getClass(), data);
	}
	
	
	public void onTracking(ServerPlayer tracking) {
		for (var listener : entityDataSync.values()) {
			listener.syncToTracking(tracking);
		}
	}
	
	public void onSyncToPlayer(ServerPlayer player) {
		for (var listener : playerDataSync.values()) {
			listener.syncToPlayer(player);
		}
	}
	
	public void onClone(Player newPlayer, boolean wasDeath) {
		for (var listener : playerDataSync.values()) {
			listener.onPlayerClone(newPlayer, wasDeath);
		}
	}
	
	public void onTick() {
		if (!pendingAddToTick.isEmpty()) {
			for (var attachment : pendingAddToTick) {
				this.ticking.put(attachment.getClass(), attachment);
			}
			pendingAddToTick.clear();
		}
		for (var listener : ticking.values()) {
			listener.tick();
		}
	}
	
	public void afterNbtRead() {
		for (var listener : postNbtCallback.values()) {
			listener.afterNbtRead();
		}
	}
	
}
