package com.github.standobyte.jojo.subsystems.entity_puppetcontrol;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public class EntityComponentController implements TickingEntityData {
	public final Entity thisEntity;
	@Nullable private LivingEntity controllingEntity;
	@Nullable private Entity controlTarget;
	public String controllerType;
	
	public EntityComponentController(Entity entity) {
		this.thisEntity = entity;
		addTicking(entity);
	}
	
	
	@Nullable
	public static Entity getControlTarget(LivingEntity controllerEntity) {
		EntityComponentController component = ComponentUtil.getExistingDataOrNull(controllerEntity, ModDataAttachmentTypes.CONTROLLER);
		return component != null ? component.controlTarget : null;
	}
	
	@Nullable
	public static EntityComponentController getCurrentController(Entity targetEntity) {
		EntityComponentController component = ComponentUtil.getExistingDataOrNull(targetEntity, ModDataAttachmentTypes.CONTROLLER);
		return component != null && component.controllingEntity != null ? component : null;
	}
		

	@Override
	public void tick() {
		if (controlTarget != null) {
			if (!controlTarget.isAlive()) {
				this.stopControlling();
				if (thisEntity.level().isClientSide() && thisEntity == ClientProxy.getClientPlayer()) {
					ClientEntityController.setInstance(null);
				}
			}
		}
		
		if (controllingEntity != null) {
			if (!controllingEntity.isAlive()) {
				EntityComponentController controllerComponent = controllingEntity.getData(ModDataAttachmentTypes.CONTROLLER);
				controllerComponent.stopControlling();
			}
		}
	}
	
	
	// TODO sync when the target entity is being newly tracked by another player
	public void setControlTarget(@Nullable Entity controlTarget, @Nullable String clientControllerType) {
		if (thisEntity instanceof LivingEntity controllingEntity) {
			if (this.controlTarget != null) {
				EntityComponentController prevTargetComponent = this.controlTarget.getData(ModDataAttachmentTypes.CONTROLLER.get());
				prevTargetComponent.controllingEntity = null;
				prevTargetComponent.controllerType = null;
				if (!thisEntity.level().isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.controlTarget, new SetClientControllerPacket(
							this.controlTarget.getId(), -1, Optional.empty()));
				}
			}
			
			if (controlTarget != null) {
				EntityComponentController newTargetComponent = controlTarget.getData(ModDataAttachmentTypes.CONTROLLER.get());
				newTargetComponent.controllingEntity = controllingEntity;
				newTargetComponent.controllerType = clientControllerType;
				if (!thisEntity.level().isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntityAndSelf(controlTarget, new SetClientControllerPacket(
							controlTarget.getId(), thisEntity.getId(), Optional.ofNullable(clientControllerType)));
				}
			}
			else if (thisEntity instanceof ServerPlayer thisPlayer) {
				PacketDistributor.sendToPlayer(thisPlayer, new SetClientControllerPacket(
						-1, thisEntity.getId(), Optional.ofNullable(clientControllerType)));
			}
			
			
			this.controlTarget = controlTarget;
			this.controllerType = clientControllerType;
		}
	}
	
	public void stopControlling() {
		setControlTarget(null, null);
	}
	
	
	public static void setControlTarget(LivingEntity controllingEntity, @Nullable Entity targetEntity, @Nullable String setOnClientType) {
		if (targetEntity != null) {
			EntityComponentController component = controllingEntity.getData(ModDataAttachmentTypes.CONTROLLER.get());
			component.setControlTarget(targetEntity, setOnClientType);
		}
		else {
			EntityComponentController component = ComponentUtil.getExistingDataOrNull(controllingEntity, ModDataAttachmentTypes.CONTROLLER);
			if (component != null) {
				component.stopControlling();
			}
		}
	}
	
	
	@Nullable
	public LivingEntity getControllingEntity() {
		return controllingEntity;
	}
	
	@Nullable
	public Entity getControlTarget() {
		return controlTarget;
	}
	
	public boolean suppressControlledEntity() {
		return true;
	}

}
