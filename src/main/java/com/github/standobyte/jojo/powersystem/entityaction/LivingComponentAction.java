package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.customobjects.LivingReactToNewAction;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionInstancePacket;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.SyncActionInstanceData;
import com.github.standobyte.jojo.subsystems.target.ActionTargetAim;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;

public class LivingComponentAction implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	private final LivingEntity entity;
	private final LivingReactToNewAction setActionCallback;
	private final AtomicInteger actionIdCounter = new AtomicInteger();
	@Nullable private EntityActionInstance action;
	
	public ActionComboStringTracker comboString = new ActionComboStringTracker();
	
	@Nullable public AnimFramePose clPrevPunchPose;
	
	// TODO move aim to a separate component
	@Deprecated
	public final ActionTargetAim entityAim;
	
	public LivingComponentAction(LivingEntity entity) {
		this.entity = entity;
		this.setActionCallback = (entity instanceof LivingReactToNewAction standEntity) ? standEntity : null;
		this.entityAim = new ActionTargetAim();
		addSynchronization(entity);
		addTicking(entity);
	}
	
	public EntityActionInstance getAction() {
		return action;
	}
	
	
	public HeldInput setAction(EntityActionInstance action, LivingEntity powerUser, SyncType sync) {
		if (action != null) {
			action.powerUser.setEntity(powerUser);
		}
		return setAction(action, sync);
	}
	
	@ApiStatus.Internal
	public HeldInput setAction(@Nullable EntityActionInstance action, SyncType sync) {
		// A way for the performer entity to the changed action, i.e. for a Stand entity to reset offset to idle
		
		if (setActionCallback != null && setActionCallback.onActionSet(action)) {
			return null;
		}
		
		// Resolve entity references
		
		if (action != null && action.standRotationTarget != null) {
			action.standRotationTarget = action.standRotationTarget.resolveEntityId(entity.level());
		}
		
		// Action callbacks that may be overriden by specific abilities
		
		if (this.action != null) {
			this.action._beforeActionRemoved(action);
		}
		EntityActionInstance prevAction = this.action;
		assignAction(action);
		if (action != null) {
			action._onActionStarted(prevAction);
		}
		
		if (entity.level().isClientSide()) {
			clPrevPunchPose = null;
			if (action != null && action.savePrevPoseForAnimTransition(prevAction) && entity instanceof AnimatedEntity entity) {
				AnimFramePose clientSavedPose = entity.jojo_ripples$getModelPose(AnimatedEntity.PoseType.UNMODIFIED);
				if (clientSavedPose != null) {
					clPrevPunchPose = clientSavedPose.deepCopy();
				}
			}
		}
		
		// Sync to players
		
		if (!entity.level().isClientSide()) {
			switch (sync) {
				case TRACKING -> PacketDistributor.sendToPlayersTrackingEntity(entity, new TrEntityActionInstancePacket(
						entity.getId(), action));
				case TRACKING_AND_SELF -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrEntityActionInstancePacket(
						entity.getId(), action));
				default -> {}
			}
		}
		
		if (action != null) {
			comboString.onNewAction(action);
		}
		else {
			comboString.clear();
		}
		
		return action;
	}
	
	private void assignAction(EntityActionInstance action) {
		if (action != null) {
			action.performer = entity;
			if (!entity.level().isClientSide()) {
				action.id = actionIdCounter.incrementAndGet() & 127;
			}
		}
		this.action = action;
	}
	
	
	@Override
	public void tick() {
		if (action != null) {
			tickAction();
			if (action != null && !entity.level().isClientSide()) {
				SyncActionInstanceData.tickSyncDirtyData(entity, action.synchedData.getDataSyncher());
			}
		}
	}
	
	protected void tickAction() {
		action._tickAction();
		if (action.isOver()) {
			setAction(null, null, SyncType.NO_SYNC);
		}
	}
	
	
	@Override
	public void syncToPlayer(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrEntityActionInstancePacket(
				entity.getId(), action));
	}

	@Override
	public void syncToTracking(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new TrEntityActionInstancePacket(
				entity.getId(), action));
		SyncActionInstanceData.onStartedTracking(player, entity, action);
	}
	
	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {}


	// TODO (entity action 2) nbt save/load
	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		
	}
	
	
	public static LivingComponentAction create(IAttachmentHolder obj) {
		if (obj instanceof LivingEntity entity) {
			return new LivingComponentAction(entity);
		}
		throw new IllegalArgumentException();
	}
	
	public static LivingComponentAction getComponent(LivingEntity entity) {
		return entity.getData(ModDataAttachmentTypes.LIVING_ACTION.get());
	}
	
	@Nullable
	public static LivingComponentAction getExistingComponent(LivingEntity entity) {
		if (entity == null) return null;
		AttachmentType<LivingComponentAction> t = ModDataAttachmentTypes.LIVING_ACTION.get();
		return entity.hasData(t) ? entity.getData(t) : null;
	}
	
	@Nullable
	public static EntityActionInstance getCurEntityAction(LivingEntity entity) {
		LivingComponentAction existingData = getExistingComponent(entity);
		return existingData != null ? existingData.getAction() : null;
	}
	
	@Nullable
	public static ActionTargetAim getAim(LivingEntity entity) {
		LivingComponentAction existingData = getExistingComponent(entity);
		return existingData != null ? existingData.entityAim : null;
	}

}
