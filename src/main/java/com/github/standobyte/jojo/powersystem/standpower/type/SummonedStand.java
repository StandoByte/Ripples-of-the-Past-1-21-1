package com.github.standobyte.jojo.powersystem.standpower.type;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;
import com.github.standobyte.jojo.entityattachment.syncheddata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacket;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacketHandler;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityUnsummonAction;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface SummonedStand {
	void setUserAndPower(LivingEntity user, StandPower power);
	default void syncTo(ServerPlayer trackingPlayer, LivingEntity user) {}
	void tickStand(LivingEntity user, StandPower userStand);
	@Nullable StandEntity getStandEntity();
	default void setSelectedSkin(Optional<ResourceLocation> skin) {}
	
	/** @return true if the Stand should unsummon right away */
	default boolean unsummonCommand() { return true; }
	default boolean isUnsummoned() { return false; }
	
	public static class BlankSummonedStand implements SummonedStand {
		protected LivingEntity user;
		protected StandPower power;
		
		@Override
		public void setUserAndPower(LivingEntity user, StandPower power) {
			this.user = user;
			this.power = power;
		}

		@Override
		public void tickStand(LivingEntity user, StandPower userStand) {}
		
		@Override
		public StandEntity getStandEntity() {
			return null;
		}
		
	}
	
	public static class SyncableSummonedStand implements SummonedStand, SyncedDataHolderExtended {
		public static final DataParameter<Boolean> IS_UNSUMMONING = DataParameter.defineId(
				SyncableSummonedStand.class, EntityDataSerializers.BOOLEAN, false);
		public static final DataParameter<Integer> UNSUMMON_LENGTH = DataParameter.defineId(
				SyncableSummonedStand.class, EntityDataSerializers.INT, 5);
		
		private static final String SYNCHED_PACKET_HANDLER_TYPE = "stand";
		static {
			SynchedDataPacket.Handler.specificHandlers.put(SYNCHED_PACKET_HANDLER_TYPE, new SynchedDataPacketHandler() {

				@Override
				public SynchedDataHelper getDataSyncHelper(Entity entity) {
					if (entity instanceof LivingEntity living) {
						StandPower standPower = StandPower.get(living);
						if (standPower != null && standPower.getSummonedStand() instanceof SyncableSummonedStand syncable) {
							return syncable.synchedData;
						}
					}
					return null;
				}

				@Override
				public SynchedDataHelper getOrCreateDataSyncHelper(Entity entity) {
					return getDataSyncHelper(entity);
				}
				
			});
		}
		
		protected LivingEntity user;
		protected StandPower power;
		public SynchedDataHelper synchedData;
		public int unsummonTimer;

		@Override
		public void defineSynchedData(Builder builder) {
			IS_UNSUMMONING.define(builder);
			UNSUMMON_LENGTH.define(builder);
		}

		@Override
		public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor) {
			if (dataAccessor == IS_UNSUMMONING.param) {
				boolean unsummon = (boolean) newValue;
				if (unsummon) {
					unsummonTimer = UNSUMMON_LENGTH.get(synchedData);
				}
				else {
					unsummonTimer = -1;
				}
			}
		}

		@Override
		public void setUserAndPower(LivingEntity user, StandPower power) {
			this.user = user;
			this.power = power;
			this.synchedData = new SynchedDataHelper(SYNCHED_PACKET_HANDLER_TYPE, this, () -> user.level().isClientSide());
		}

		@Override
		public void tickStand(LivingEntity user, StandPower userStand) {
			if (isBeingUnsummoned()) {
				if (unsummonTimer > 0) --unsummonTimer;
			}
			
			if (user != null && !user.level().isClientSide()) {
				SynchedDataExtended.tickSyncDirtyData(synchedData.getDataSyncher(), user);
			}
		}

		@Override
		public StandEntity getStandEntity() {
			return null;
		}
		
		@Override
		public boolean unsummonCommand() {
			if (user != null && !user.level().isClientSide()) {
				IS_UNSUMMONING.set(synchedData, true);
			}
			return false;
		}

		@Override
		public boolean isUnsummoned() {
			return isBeingUnsummoned() && unsummonTimer <= 0;
		}
		
		public boolean isBeingUnsummoned() {
			return IS_UNSUMMONING.get(synchedData);
		}
		
		public float unsummonAlpha(float partialTick) {
			int unsummonLength = UNSUMMON_LENGTH.get(synchedData);
            float alpha = isBeingUnsummoned() ? StandEntityUnsummonAction.alpha(
            		unsummonLength - unsummonTimer + partialTick, 
            		unsummonLength) : 1;
            return alpha;
		}
		
	}
	
}
