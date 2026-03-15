package com.github.standobyte.jojo.entityattachment.custom_effect.sync;

import java.util.List;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsClass;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectsMap;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataExtended;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class SyncStandEffectInstanceData {
	
	public static void onStartedTracking(ServerPlayer tracking, Entity standUser, 
			EntityCustomEffectsClass effectsClass, EntityCustomEffect effect) {
		SynchedDataExtended synchedData = effect.synchedData.getDataSyncher();
		if (synchedData != null) {
			List<SynchedEntityData.DataValue<?>> nonDefaultData = synchedData.syncOnStartedTracking();
			if (nonDefaultData != null) {
				PacketDistributor.sendToPlayer(tracking, new TrStandEffectSynchedDataPacket(standUser.getId(), effect.getId(), effectsClass, nonDefaultData));
			}
		}
	}
	
	public static void tickSyncDirtyData(Entity standUser, EntityCustomEffectsClass effectsClass, EntityCustomEffect effect) {
		SynchedDataExtended synchedData = effect.synchedData.getDataSyncher();
		if (synchedData != null) {
			List<SynchedEntityData.DataValue<?>> dirtyData = synchedData.syncDirtyData();
			if (dirtyData != null) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(standUser, new TrStandEffectSynchedDataPacket(standUser.getId(), effect.getId(), effectsClass, dirtyData));
			}
		}
	}
	
	public static void setDataClientSide(Entity entity, int effectId, 
			EntityCustomEffectsClass effectsClass, List<SynchedEntityData.DataValue<?>> packedItems) {
		EntityCustomEffectsMap<?> standEffects = effectsClass.get(entity, false);
		if (standEffects != null) {
			EntityCustomEffect effect = standEffects.getById(effectId);
			if (effect != null) {
				SynchedDataExtended synchedData = effect.synchedData.getDataSyncher();
				if (synchedData != null) {
					synchedData.assignValues(packedItems);
				}
			}
		}
	}
	
}
