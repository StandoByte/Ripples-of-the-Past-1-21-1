package com.github.standobyte.jojo.util.syncheddata;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class SynchedDataHelper {
	protected HasLevelReference entityLikeObject;
	protected String objClassName;
	protected boolean clientSide;
	
	@Nullable protected SynchedDataExtended synchedData;
	protected boolean didLazyInit;
	
	public SynchedDataHelper(HasLevelReference entityLikeObject) {
		this.entityLikeObject = entityLikeObject;
		this.objClassName = entityLikeObject.getClass().getName();
	}
	
	@Nullable
	@ApiStatus.NonExtendable
	public SynchedDataExtended getDataSyncher() {
		if (synchedData == null && !didLazyInit) {
			clientSide = entityLikeObject.isClientSide();
			if (entityLikeObject instanceof SyncedDataHolderExtended withSynchedData) {
				SynchedEntityData.Builder builder = new SynchedEntityData.Builder(withSynchedData);
				withSynchedData.defineSynchedData(builder);
				synchedData = new SynchedDataExtended(builder, clientSide);
			}
			entityLikeObject = null; // we don't need this reference anymore
			didLazyInit = true;
		}
		return synchedData;
	}
	
	public <T> T get(EntityDataAccessor<T> key) {
		hasSynchedDataCheck();
		return getDataSyncher().get(key);
	}

	public <T> void set(EntityDataAccessor<T> key, T value) {
		hasSynchedDataCheck();
		set(key, value, false);
	}

	public <T> void set(EntityDataAccessor<T> key, T value, boolean forceUpdate) {
		hasSynchedDataCheck();
		getDataSyncher().set(key, value, forceUpdate);
	}
	
	protected void hasSynchedDataCheck() {
		if (didLazyInit && synchedData == null) {
			throw new ClassCastException("Object of class " + objClassName + " does not implement SyncedDataHolderExtended");
		}
	}

}
