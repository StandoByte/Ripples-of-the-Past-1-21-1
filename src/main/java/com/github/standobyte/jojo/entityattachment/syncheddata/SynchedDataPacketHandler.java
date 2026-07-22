package com.github.standobyte.jojo.entityattachment.syncheddata;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;

public interface SynchedDataPacketHandler {
	@Nullable SynchedDataHelper getDataSyncHelper(Entity entity);
	@Nullable SynchedDataHelper getOrCreateDataSyncHelper(Entity entity);
}
