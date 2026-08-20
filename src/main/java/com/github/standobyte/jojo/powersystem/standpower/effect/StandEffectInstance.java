package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.entityattachment.custom_effect.TrEntityCustomEffectsPacket;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class StandEffectInstance extends EntityCustomEffect {
	protected StandPower userPower;

	private Entity target;
	private LivingEntity targetLiving;
	private UUID targetUUID;
	private int targetNetworkId = -1;
	
	public boolean removeOnStandChanged = true;
	public boolean needsTarget = false;
	
	public boolean isFromStandAction = false;
	public EntityActionInstance standAction;
	public boolean isFromUserAction = false;
	public EntityActionInstance userAction;


	public StandEffectInstance(@Nonnull EntityCustomEffectType<?> effectType) {
		super(effectType);
		removeOnUserLogout = true;
	}
	
	protected void initStandPower(StandPower userPower) {
		this.userPower = userPower;
		if (userPower == null) {
			JojoMod.getLogger().warn("userPower was null when initializing a {} effect!", this.getClass().getSimpleName());
			return;
		}
		
		if (isFromStandAction) {
			StandEntity standEntity = userPower.getSummonedStandEntity();
			if (standEntity != null) standAction = LivingComponentAction.getCurEntityAction(standEntity);
			if (standAction != null) standAction.getPunchModifiers().add(this);
		}
		if (isFromUserAction) {
			LivingEntity user = userPower.getUser();
			if (user != null) userAction = LivingComponentAction.getCurEntityAction(user);
			if (userAction != null) userAction.getPunchModifiers().add(this);
		}
	}

	@Override
	public StandEffectInstance withEntity(Entity entity) {
		super.withEntity(entity);
		initStandPower(StandPower.get((LivingEntity) entity));
		return this;
	}

	public StandEffectInstance withStand(StandPower stand) {
		super.withEntity(stand.getUser());
		initStandPower(stand);
		return this;
	}

	public StandEffectInstance withTarget(Entity target) {
		this.target = target;
		this.targetLiving = target instanceof LivingEntity ? (LivingEntity) target : null;
		this.targetUUID = target != null ? target.getUUID() : null;
		this.targetNetworkId = target.getId();
		return this;
	}

	public StandEffectInstance withTargetEntityId(int entityId) {
		this.targetNetworkId = entityId;
		if (target != null && target.getId() != entityId) {
			this.target = null;
			this.targetLiving = null;
		}
		return this;
	}

	public StandPower getUserPower() {
		return userPower;
	}

	public LivingEntity getStandUser() {
		return (LivingEntity) getEntity();
	}

	public Entity getTarget() {
		return target;
	}

	public LivingEntity getTargetLiving() {
		return targetLiving;
	}

	public UUID getTargetUUID() {
		return targetUUID;
	}

	@Override
	public void onStart() {
		if (!level.isClientSide() && targetLiving != null) {
			StandEffectsTarget targetEffects = StandEffectsTarget.getList(targetLiving);
			if (targetEffects != null) {
				targetEffects.addEffectTargetedBy(this);
			}
		}
		super.onStart();
	}

	@Override
	public void onTick() {
		if (!toBeRemoved) {
			updateTarget(level);
			if (targetUUID == null && needsTarget) {
				if (!level.isClientSide()) remove();
				return;
			}
		}
		
		super.onTick();
	}

	public void updateTarget(Level level) {
		if (target == null) {
			if (!level.isClientSide()) {
				if (targetUUID != null) {
					Entity entity = ((ServerLevel) level).getEntity(targetUUID);
					setTargetEntity(entity);
				}
			}
			else if (targetNetworkId > -1) {
				Entity entity = level.getEntity(targetNetworkId);
				setTargetEntity(entity);
			}
		}

		if (!level.isClientSide() && targetUUID != null && 
				target != null && shouldClearTarget(target, targetLiving)) {
			clearTarget();
		}

		if (target != null && !target.isAlive()) {
			setTargetEntity(null);
		}
	}

	protected final void clearTarget() {
		targetUUID = null;
		setTargetEntity(null);
	}

	public void setTargetEntity(Entity target) {
		if (this.target != target) {
			if (this.targetLiving != null) {
				StandEffectsTarget oldTargetEffects = StandEffectsTarget.getList(this.targetLiving);
				if (oldTargetEffects != null) {
					oldTargetEffects.removeEffectTargetedBy(this);
				}
			}
			this.target = target;
			if (target != null) {
				this.targetUUID = target.getUUID();
			}
			if (target instanceof LivingEntity newTargetLiving) {
				StandEffectsTarget targetEffects = StandEffectsTarget.getList(newTargetLiving);
				if (targetEffects != null) {
					targetEffects.addEffectTargetedBy(this);
				}
				this.targetLiving = newTargetLiving;
			}
			else {
				this.targetLiving = null;
			}

			if (!level.isClientSide()) {
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(getEntity(), TrEntityCustomEffectsPacket.updateTarget(this));
			}
		}
	}
	
	public int getTargetEntityId() {
		return Optional.ofNullable(this.getTarget()).map(Entity::getId).orElse(-1);
	}

	@Override
	public void onStop() {
		if (targetLiving != null) {
			StandEffectsTarget targetEffects = StandEffectsTarget.getList(targetLiving);
			if (targetEffects != null) {
				targetEffects.removeEffectTargetedBy(this);
			}
		}
		super.onStop();
	}

	protected abstract void start();
	protected abstract void tick();
	protected abstract void stop();
	
	protected boolean shouldClearTarget(Entity target, @Nullable LivingEntity targetLiving) {
		return targetLiving != null && targetLiving.isDeadOrDying();
	}

	@Override
	public void syncWithUserOnly(ServerPlayer user) {
		super.syncWithUserOnly(user);
		updateTarget(user.level());
	}

	@Override
	public void syncWithTrackingOrUser(ServerPlayer user) {
		super.syncWithTrackingOrUser(user);
	}

	@Override
	public void writeAdditionalPacketData(FriendlyByteBuf buf, boolean sendingToUser) {
		super.writeAdditionalPacketData(buf, sendingToUser);
		buf.writeInt(getTargetEntityId());
	}

	@Override
	public void readAdditionalPacketData(FriendlyByteBuf buf, boolean clientIsUser) {
		super.readAdditionalPacketData(buf, clientIsUser);
		int targetEntityId = buf.readInt();
		if (targetEntityId != -1) {
			this.withTargetEntityId(targetEntityId);
			this.updateTarget(level);
		}
	}

	@Override
	protected void writeAdditionalSaveData(CompoundTag nbt) {
		if (targetUUID != null) {
			nbt.putUUID("Target", targetUUID);
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		if (nbt.hasUUID("Target")) {
			this.targetUUID = nbt.getUUID("Target");
		}
	}
}
