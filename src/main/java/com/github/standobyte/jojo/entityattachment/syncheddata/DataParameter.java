package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.function.Supplier;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;

public class DataParameter<T> {
	public final EntityDataAccessor<T> param;
	public final T defaultValue;

	// defineId called for: class /*clazz*/ from class com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter
	// ^ This message is fine, but you now have to be careful with the clazz argument once again
	public static <T> DataParameter<T> defineId(Class<? extends SyncedDataHolder> clazz, EntityDataSerializer<T> serializer, T defaultValue) {
		EntityDataAccessor<T> param = SynchedEntityData.defineId(clazz, serializer);
		return new DataParameter<>(param, defaultValue);
	}

	protected DataParameter(EntityDataAccessor<T> param, T defaultValue) {
		this.param = param;
		this.defaultValue = defaultValue;
	}
	
	public void define(SynchedEntityData.Builder builder) {
		builder.define(param, defaultValue);
	}
	
	public T get(Supplier<SynchedDataHelper> getDataSyncher) {
		SynchedDataHelper dataSyncher = getDataSyncher.get();
		return dataSyncher != null ? dataSyncher.get(param) : defaultValue;
	}
	
}
