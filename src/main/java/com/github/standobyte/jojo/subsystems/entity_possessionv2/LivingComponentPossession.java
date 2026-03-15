package com.github.standobyte.jojo.subsystems.entity_possessionv2;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.SynchronizableEntityData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class LivingComponentPossession implements TickingEntityData, SynchronizableEntityData {
	private final Entity thisEntity;
	private Set<LivingComponentPossession> possessingEntities = new HashSet<>();
	private Entity possessTarget = null;
	public String possessionType;
	
	public LivingComponentPossession(Entity entity) {
		this.thisEntity = entity;
		addTicking(entity);
		addSynchronization(entity);
	}
	
	
	@Nullable
	public static Entity getEntityPossessedBy(Entity possessing) {
		AttachmentType<LivingComponentPossession> type = ModDataAttachmentTypes.ENTITY_POSSESSION.get();
		if (!possessing.hasData(type)) return null;
		
		LivingComponentPossession possessing_ = possessing.getData(type);
		return possessing_.possessTarget;
	}
	
	public static boolean isPossessingSomeone(Entity entity) {
		return getEntityPossessedBy(entity) != null;
	}
	
	@Nullable
	public static Set<LivingComponentPossession> getEntitiesPossessing(Entity target) {
		AttachmentType<LivingComponentPossession> type = ModDataAttachmentTypes.ENTITY_POSSESSION.get();
		if (!target.hasData(type)) return null;
		
		LivingComponentPossession target_ = target.getData(type);
		return target_.possessingEntities;
	}
	
	
	@Override
	public void tick() {
		if (possessTarget != null) {
			if (!possessTarget.isAlive()) {
				this.setPossessionTarget(null, null);
			}
		}
		
		updatePosition();
	}
	
	public void updatePosition() {
		if (possessTarget != null) {
			thisEntity.noPhysics = true;
			thisEntity.setOnGround(false);
			thisEntity.fallDistance = 0;
			
			thisEntity.absMoveTo(
					possessTarget.getX(), 
					possessTarget.getY(), 
					possessTarget.getZ(), 
					possessTarget.getYRot(), 
					possessTarget.getXRot());
			thisEntity.xRotO = possessTarget.xRotO;
			thisEntity.yRotO = possessTarget.yRotO;
			LivingEntity thisAsLiving = (LivingEntity) thisEntity;
			if (possessTarget instanceof LivingEntity livingTarget) {
				thisAsLiving.yHeadRot = livingTarget.yHeadRot;
				thisAsLiving.yBodyRot = livingTarget.yBodyRot;
				thisAsLiving.yHeadRotO = livingTarget.yHeadRotO;
				thisAsLiving.yBodyRotO = livingTarget.yBodyRotO;
			}
			else {
				thisAsLiving.yHeadRot = thisAsLiving.getYRot();
				thisAsLiving.yBodyRot = thisAsLiving.getYRot();
			}
			
			if (thisEntity instanceof ServerPlayer serverPlayer) {
				serverPlayer.serverLevel().getChunkSource().move(serverPlayer);
			}
		}
	}
	
	public static void setPossessionTarget(LivingEntity possessing, @Nullable Entity target, @Nullable String possessionType) {
		LivingComponentPossession data =  possessing.getData(ModDataAttachmentTypes.ENTITY_POSSESSION.get());
		data.setPossessionTarget(target, possessionType);
	}
	
	public void stopPossession() {
		setPossessionTarget(null, null);
	}
	
	public void setPossessionTarget(@Nullable Entity target, @Nullable String possessionType) {
		while (target instanceof PartEntity<?> partEntity) target = partEntity.getParent();
		
		if (this.possessTarget != target) {
			if (this.possessTarget != null) {
				LivingComponentPossession oldTargetData = this.possessTarget.getData(ModDataAttachmentTypes.ENTITY_POSSESSION.get());
				oldTargetData.possessingEntities.remove(this);
				// TODO sync
			}
			
			if (target != null) {
				LivingComponentPossession newTargetData = target.getData(ModDataAttachmentTypes.ENTITY_POSSESSION.get());
				newTargetData.possessingEntities.add(this);
				// TODO sync
			}
		}
		
		this.possessTarget = target;
		this.possessionType = possessionType;
		if (!thisEntity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(thisEntity, new TrPossessEntityPacket(
					thisEntity.getId(), possessTarget != null ? possessTarget.getId() : 0, possessionType));
		}
		
		if (target != null) {
			thisEntity.stopRiding();
			
			if (thisEntity.level() instanceof ServerLevel serverLevel) {
				thisEntity.teleportTo(serverLevel, 
						target.getX(), target.getY(), target.getZ(), 
						Set.of(), target.getYRot(), target.getXRot());
				if (thisEntity instanceof ServerPlayer serverPlayer) {
					serverLevel.getChunkSource().move(serverPlayer);
					serverPlayer.connection.send(new ClientboundSetCameraPacket(target));
					serverPlayer.connection.resetPosition();
				}
			}
		}
		else {
			thisEntity.noPhysics = false;
			if (thisEntity instanceof ServerPlayer serverPlayer) {
				serverPlayer.connection.send(new ClientboundSetCameraPacket(serverPlayer));
			}
		}
	}
	
	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		if (possessTarget != null) {
			PacketDistributor.sendToPlayer(trackingPlayer, new TrPossessEntityPacket(
					thisEntity.getId(), possessTarget != null ? possessTarget.getId() : 0, possessionType));
		}
		// TODO sync being possessed
	}
	
}
