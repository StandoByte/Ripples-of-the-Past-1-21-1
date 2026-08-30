package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.function.Supplier;

import com.github.standobyte.jojo.init.ModEntityDataSerializers;

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
	
	/** @deprecated Why does this exist?? Я че пьяный? */
	@Deprecated
	public T get(Supplier<SynchedDataHelper> getDataSyncher) {
		SynchedDataHelper dataSyncher = getDataSyncher.get();
		return get(dataSyncher);
	}
	
	public T get(SynchedDataHelper dataSyncher) {
		return dataSyncher != null ? dataSyncher.get(param) : defaultValue;
	}
	
	public void set(SynchedDataHelper dataSyncher, T value) {
		dataSyncher.set(param, value);
	}
	
	
	/** Utilizes the synched data system for sending a packet from server to tracking clients every time a certain event happens. */
	public static DataParameter<?> defineIdSignal(Class<? extends SyncedDataHolder> clazz) {
		return defineId(clazz, ModEntityDataSerializers.SIGNAL.get(), _SIGNAL_DUMMY);
	}
	
	@SuppressWarnings("unchecked")
	public void sendSignal(SynchedDataHelper dataSyncher) {
		if (this.defaultValue != _SIGNAL_DUMMY) {
			throw new RuntimeException("sendSignal called on a non-signal data parameter.");
		}
		dataSyncher.set((EntityDataAccessor<Object>) param, _SIGNAL_DUMMY, 
				true /* By default the system doesn't send a packet to clients if the underlying value hasn't changed. 
						We aren't storing any value in this case, we just want to send the packet every time this method is called. */);
	}
	
	
	public static final Object _SIGNAL_DUMMY = new Object();
	
}
