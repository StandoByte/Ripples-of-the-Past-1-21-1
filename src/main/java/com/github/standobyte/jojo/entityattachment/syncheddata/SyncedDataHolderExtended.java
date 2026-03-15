package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.List;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;

public interface SyncedDataHolderExtended extends SyncedDataHolder {
	@Override default void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {}
	@Override default void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> newData) {}

	void defineSynchedData(SynchedEntityData.Builder builder);
	<T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor);
}
