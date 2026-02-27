package com.github.standobyte.jojo.powersystem.entityaction.syncdata;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.util.syncheddata.SynchedDataExtended;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class SyncActionInstanceData {

	@SubscribeEvent
	public static void onStartedTracking(PlayerEvent.StartTracking event) {
		Entity entity = event.getTarget();
		SynchedDataExtended synchedData = getActionSynchedData(entity);
		if (synchedData != null) {
			List<SynchedEntityData.DataValue<?>> nonDefaultData = synchedData.syncOnStartedTracking();
			if (nonDefaultData != null) {
				ServerPlayer tracking = (ServerPlayer) event.getEntity();
				PacketDistributor.sendToPlayer(tracking, new TrActionSynchedDataPacket(entity.getId(), nonDefaultData));
			}
		}
	}
	
	@Nullable
	public static void tickSyncDirtyData(Entity entity, SynchedDataExtended synchedData) {
		if (synchedData != null) {
			List<SynchedEntityData.DataValue<?>> dirtyData = synchedData.syncDirtyData();
			if (dirtyData != null) {
				PacketDistributor.sendToPlayersTrackingEntity(entity, new TrActionSynchedDataPacket(entity.getId(), dirtyData));
			}
		}
	}
	
	public static void setDataClientSide(LivingEntity entity, List<SynchedEntityData.DataValue<?>> packedItems) {
		SynchedDataExtended synchedData = getActionSynchedData(entity);
		if (synchedData != null) {
			synchedData.assignValues(packedItems);
		}
	}
	
	@Nullable
	public static SynchedDataExtended getActionSynchedData(Entity entity) {
		if (entity instanceof LivingEntity living) {
			EntityActionInstance action = LivingComponentAction.getCurEntityAction(living);
			if (action != null) {
				return action.synchedData.getDataSyncher();
			}
		}
		return null;
	}
	
}
