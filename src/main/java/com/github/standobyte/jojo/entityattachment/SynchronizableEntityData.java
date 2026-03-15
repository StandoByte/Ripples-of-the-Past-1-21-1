package com.github.standobyte.jojo.entityattachment;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * An interface to synchronize the data attached to an entity with 1 line of code
 * ({@link SynchronizableEntityData#addSynchronization(Entity)}), instead of having to add it into an event handler.
 */
public interface SynchronizableEntityData {
	void syncToTracking(ServerPlayer trackingPlayer);
	
	/**
	 * Call this inside the constructor of your data class that you're attaching to an entity,
	 * and the event handler inside the main mod will take care of the synchronization, 
	 * using the methods above.
	 */
	default void addSynchronization(Entity entity) {
		entity.getData(ModDataAttachmentTypes.DATA_EVENT_HELPER.get()).addEntityDataSync(this);
	}
}
