package com.github.standobyte.jojo.powersystem.entityaction.syncdata;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.syncher.SynchedEntityData;

public class SynchedDataWrapper {
	public final SynchedEntityData data;
	public List<SynchedEntityData.DataValue<?>> serverTrackedDataValues;
	
	public SynchedDataWrapper(SynchedEntityData data, boolean clientSide) {
		this.data = data;
		if (!clientSide) {
			serverTrackedDataValues = data.getNonDefaultValues();
		}
	}
	
	@Nullable
	public List<SynchedEntityData.DataValue<?>> syncOnStartedTracking() {
		return serverTrackedDataValues;
	}
	
	@Nullable
	public List<SynchedEntityData.DataValue<?>> syncDirtyData() {
		List<SynchedEntityData.DataValue<?>> dirty = data.packDirty();
		if (dirty != null) {
			serverTrackedDataValues = data.getNonDefaultValues();
		}
		return dirty;
	}
}
