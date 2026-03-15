package com.github.standobyte.jojo.entityattachment;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * An extension of {@link SynchronizableEntityData} with methods to sync the data to the player that holds it.
 * If your data class is intended to also be attached to players and sync some of the data exclusively with the player, 
 * implement this interface.
 */
public interface SynchronizablePlayerData extends SynchronizableEntityData {
	void syncToPlayer(ServerPlayer entityAsPlayer);
	void onPlayerClone(Player newPlayer, boolean wasDeath);
	
	/**
	 * Call this inside the constructor of your data class that you're attaching to an entity,
	 * and the event handler inside the main mod will take care of the synchronization, 
	 * using the methods above.
	 */
	@Override
	default void addSynchronization(Entity entity) {
		entity.getData(ModDataAttachmentTypes.DATA_EVENT_HELPER.get()).addPlayerDataSync(this);
	}
}
