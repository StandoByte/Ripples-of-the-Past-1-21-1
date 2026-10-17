package com.github.standobyte.jojo.util.objects_mc;

import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;

public class EntityResolverSyncable extends EntityResolver {
	protected SynchedEntityData entitySynchedData;
	protected EntityDataAccessor<Integer> intIdAccessor;

	public EntityResolverSyncable(SynchedEntityData entitySynchedData, EntityDataAccessor<Integer> intIdStaticAccessor) {
		this.entitySynchedData = entitySynchedData;
		this.intIdAccessor = intIdStaticAccessor;
	}

	
	public static DataParameter<Integer> defineEntityIdStaticAccessor(Class<? extends SyncedDataHolder> entityClass) {
		return DataParameter.defineId(entityClass, EntityDataSerializers.INT, EMPTY_NETWORK_ID);
	}
	
	/** Call this manually in your entity's {@link SyncedDataHolder#onSyncedDataUpdated(EntityDataAccessor)} override. */
	public boolean onSynchedDataUpdated(EntityDataAccessor<?> updatedKey) {
		if (updatedKey == this.intIdAccessor) {
			this.entityNetworkId = entitySynchedData.get(intIdAccessor);
			return true;
		}
		return false;
	}
	
	@Override
	protected void _assignNetworkId(int id) {
		entitySynchedData.set(intIdAccessor, id);
	}
}
