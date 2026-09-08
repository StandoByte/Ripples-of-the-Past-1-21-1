package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsClass;
import com.github.standobyte.jojo.entityattachment.custom_effect.sync.TrStandEffectSynchedDataPacket;
import com.github.standobyte.jojo.init.ModEntityDataSerializers;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.TrActionSynchedDataPacket;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

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
	

	/** Utilizes the synched data system (and the regular parameters from {@link #defineId(Class, EntityDataSerializer, Object)}), 
	 *  but, instead of storing the data and only sending it to clients on the next tick if the value has changed, 
	 *  sends the value to clients immediately, and does not store the sent value 
	 *  (therefore not sending the last value to the newly tracking players).
	 *  The value itself is to be handled in {@link SyncedDataHolderExtended#onSyncedDataUpdated(Object, Object, EntityDataAccessor)}.
	 * 
	 *  Example use case is for creating specific visuals/sounds on the client side whenever a specific event happens on the server side,
	 *  so that there is no need to make a separate packet for it.
	 */
	public void sendSignal(SynchedDataHelper dataSyncher, T value) {
		dataSyncher.hasSynchedDataCheck();
		
		SynchedDataExtended delegate = dataSyncher.getDataSyncher();
		delegate.entity2.onSyncedDataUpdated(defaultValue, value, param);
		
		SynchedEntityData.DataValue<T> valueToSync = SynchedEntityData.DataValue.create(param, value);
		List<SynchedEntityData.DataValue<?>> data = Collections.singletonList(valueToSync);
		Entity entity = dataSyncher.entity;
		CustomPacketPayload packet = switch (delegate.entity2) { // this shit SO ass
			case EntityActionInstance action ->  new TrActionSynchedDataPacket(
					entity.getId(), 
					data);
			case EntityCustomEffect effect -> new TrStandEffectSynchedDataPacket(
					entity.getId(), 
					effect.getId(), 
					effect instanceof StandEffectInstance ? EntityCustomEffectsClass.STAND_EFFECT : EntityCustomEffectsClass.OTHER, 
					data);
			default -> new SynchedDataPacket(
					entity.getId(), 
					delegate.type, 
					data);
		};
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
	}
	
	/** Same principle as above, for cases when we don't need to send any data for the signal, 
	 *  only a packet telling the tracking clients that the event has occured.
	 */
	public static DataParameter<?> defineIdEmptySignal(Class<? extends SyncedDataHolder> clazz) {
		return defineId(clazz, ModEntityDataSerializers.SIGNAL.get(), _SIGNAL_DUMMY);
	}
	public static final Object _SIGNAL_DUMMY = new Object();
	
	@SuppressWarnings("unchecked")
	public void sendEmptySignal(SynchedDataHelper dataSyncher) {
		sendSignal(dataSyncher, (T) _SIGNAL_DUMMY);
	}
	
}
