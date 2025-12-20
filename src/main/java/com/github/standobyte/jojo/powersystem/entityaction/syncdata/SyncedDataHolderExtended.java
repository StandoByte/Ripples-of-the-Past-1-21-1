package com.github.standobyte.jojo.powersystem.entityaction.syncdata;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;

public interface SyncedDataHolderExtended extends SyncedDataHolder {
	
	default <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor) {
		this.onSyncedDataUpdated(dataAccessor);
	}
}
