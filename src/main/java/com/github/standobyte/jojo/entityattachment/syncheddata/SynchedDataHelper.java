package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

public class SynchedDataHelper {
	protected Object entityLikeObject;
	protected BooleanSupplier clientSideCheck;
	
	protected boolean clientSide;
	protected String objClassName;
	
	@Nullable protected SynchedDataExtended synchedData;
	protected boolean didLazyInit;
	
	/**
	 * @param entityLikeObject should implement {@link SyncedDataHolderExtended}
	 * @param isClientSide
	 */
	public SynchedDataHelper(Object entityLikeObject, BooleanSupplier isClientSide) {
		this.entityLikeObject = entityLikeObject;
		this.clientSideCheck = isClientSide;
		this.objClassName = entityLikeObject.getClass().getName();
	}
	
	@Nullable
	@ApiStatus.NonExtendable
	public SynchedDataExtended getDataSyncher() {
		if (synchedData == null && !didLazyInit) {
			clientSide = clientSideCheck.getAsBoolean();
			if (entityLikeObject instanceof SyncedDataHolderExtended withSynchedData) {
				SynchedEntityData.Builder builder = new SynchedEntityData.Builder(withSynchedData);
				withSynchedData.defineSynchedData(builder);
				synchedData = new SynchedDataExtended(builder, clientSide);
			}
			didLazyInit = true;
			
			// we don't need this stuff anymore
			entityLikeObject = null;
			clientSideCheck = null;
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
