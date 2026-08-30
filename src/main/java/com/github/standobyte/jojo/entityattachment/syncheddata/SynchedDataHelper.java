package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public class SynchedDataHelper {
	public final String type;
	
	protected Object entityLikeObject;
	protected Supplier<Entity> getEntity;
	protected Entity entity;
	
	protected boolean clientSide;
	protected String objClassName;
	
	@Nullable protected SynchedDataExtended synchedData;
	protected boolean didLazyInit;
	
	/**
	 * @param packetHandlerType
	 * @param entityLikeObject should implement {@link SyncedDataHolderExtended}
	 * @param isClientSide
	 */
	public SynchedDataHelper(String packetHandlerType, Object entityLikeObject, Supplier<Entity> getEntity) {
		this.type = packetHandlerType;
		this.entityLikeObject = entityLikeObject;
		this.getEntity = getEntity;
		this.objClassName = entityLikeObject.getClass().getName();
	}
	
	@Nullable
	@ApiStatus.NonExtendable
	public SynchedDataExtended getDataSyncher() {
		if (synchedData == null && !didLazyInit) {
			entity = getEntity.get();
			clientSide = entity.level().isClientSide();
			if (entityLikeObject instanceof SyncedDataHolderExtended withSynchedData) {
				SynchedEntityData.Builder builder = new SynchedEntityData.Builder(withSynchedData);
				withSynchedData.defineSynchedData(builder);
				synchedData = new SynchedDataExtended(builder, type, clientSide);
			}
			didLazyInit = true;
			
			// we don't need this stuff anymore
			entityLikeObject = null;
			getEntity = null;
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
