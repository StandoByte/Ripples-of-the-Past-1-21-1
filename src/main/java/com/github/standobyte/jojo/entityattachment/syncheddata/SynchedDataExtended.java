package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class SynchedDataExtended extends SynchedEntityData {
	public final String type;
	protected final SyncedDataHolderExtended entity2;
	public List<SynchedEntityData.DataValue<?>> serverTrackedDataValues;

	public SynchedDataExtended(SyncedDataHolderExtended entity, String type, 
			SynchedEntityData.DataItem<?>[] itemsById, boolean clientSide) {
		super(entity, itemsById);
		this.entity2 = entity;
		if (!clientSide) {
			serverTrackedDataValues = this.getNonDefaultValues();
		}
		this.type = type;
	}

	public SynchedDataExtended(SynchedEntityData.Builder builder, String type, boolean clientSide) {
		this((SyncedDataHolderExtended) builder.entity, type, builder.itemsById, clientSide);
	}

	@Nullable
	public List<SynchedEntityData.DataValue<?>> syncOnStartedTracking() {
		return serverTrackedDataValues;
	}

	@Nullable
	public List<SynchedEntityData.DataValue<?>> syncDirtyData() {
		List<SynchedEntityData.DataValue<?>> dirty = this.packDirty();
		if (dirty != null) {
			serverTrackedDataValues = this.getNonDefaultValues();
		}
		return dirty;
	}
	
	
	public static void tickSyncDirtyData(SynchedDataExtended helper, Entity entity) {
		List<SynchedEntityData.DataValue<?>> dirtyData = helper.syncDirtyData();
		if (dirtyData != null) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, 
					new SynchedDataPacket(entity.getId(), helper.type, dirtyData));
		}
	}


	@Override
	public <T> void set(EntityDataAccessor<T> key, T value, boolean force) {
		SynchedEntityData.DataItem<T> dataItem = this.getItem(key);
		if (force || ObjectUtils.notEqual(value, dataItem.getValue())) {
			T oldValue = dataItem.getValue();
			dataItem.setValue(value);
			this.entity2.onSyncedDataUpdated(oldValue, value, key);
			dataItem.setDirty(true);
			this.isDirty = true;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void assignValues(List<SynchedEntityData.DataValue<?>> entries) {
		for (SynchedEntityData.DataValue<?> sentData : entries) {
			SynchedEntityData.DataItem<?> dataItem = this.itemsById[sentData.id()];
			Object oldValue = dataItem.getValue();
			this.assignValue(dataItem, sentData);
			EntityDataAccessor accessor = dataItem.getAccessor();
			this.entity2.onSyncedDataUpdated(oldValue, dataItem.getValue(), accessor);
		}

		this.entity2.onSyncedDataUpdated(entries);
	}
}
