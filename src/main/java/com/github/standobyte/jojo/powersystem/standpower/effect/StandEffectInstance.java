package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public abstract class StandEffectInstance {
	@Nonnull public final StandEffectType<?> effectType;

	private int id;
	public int tickCount = 0;
	private boolean toBeRemoved = false;

	protected LivingEntity user;
	public Level level;
	protected StandPower userPower;

	private Entity target;
	private LivingEntity targetLiving;
	private UUID targetUUID;
	private int targetNetworkId = -1;
	
	public boolean removeOnUserDeath = true;
	public boolean removeOnUserLogout = true;
	public boolean removeOnStandChanged = true;
	public boolean needsTarget = false;
	
	public boolean isFromStandAction = false;
	public EntityActionInstance standAction;
	public boolean isFromUserAction = false;
	public EntityActionInstance userAction;


	public StandEffectInstance(@Nonnull StandEffectType<?> effectType) {
		this.effectType = effectType;
	}
	
	protected void initStandPower(StandPower userPower) {
		this.userPower = userPower;
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

	public StandEffectInstance withUser(LivingEntity user) {
		this.user = user;
		this.level = user.level();
		initStandPower(StandPower.get(user));
		return this;
	}

	public StandEffectInstance withStand(StandPower stand) {
		this.user = stand.getUser();
		this.level = user.level();
		initStandPower(stand);
		return this;
	}

	public StandEffectInstance withId(int id) {
		this.id = id;
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
		return user;
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

	public void onStart() {
		if (targetLiving != null) {
			StandEffectsTarget targetEffects = StandEffectsTarget.getList(targetLiving);
			if (targetEffects != null) {
				targetEffects.addEffectTargetedBy(this);
			}
		}
		start();
	}

	public void onTick() {
		if (!toBeRemoved) {
			tickCount++;

			updateTarget(level);

			if (targetUUID == null && needsTarget) {
				if (!level.isClientSide()) remove();
				return;
			}

			tick();
		}
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
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, TrStandEffectPacket.updateTarget(this));
			}
		}
	}

	public void onStop() {
		if (targetLiving != null) {
			StandEffectsTarget targetEffects = StandEffectsTarget.getList(targetLiving);
			if (targetEffects != null) {
				targetEffects.removeEffectTargetedBy(this);
			}
		}
		toBeRemoved = true;
		stop();
	}

	protected abstract void start();
	protected abstract void tick();
	protected abstract void stop();

	protected boolean shouldClearTarget(Entity target, @Nullable LivingEntity targetLiving) {
		return targetLiving != null && targetLiving.isDeadOrDying();
	}

	public int getId() {
		return id;
	}

	public void remove() {
		toBeRemoved = true;
	}

	public boolean isStopped() {
		return toBeRemoved;
	}

	public void syncWithUserOnly(ServerPlayer user) {
		updateTarget(user.level());
	}

	public void syncWithTrackingOrUser(ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, TrStandEffectPacket.add(this, player == user));
	}

	public CompoundTag toNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", effectType.registryKey.toString());
		nbt.putInt("TickCount", tickCount);
		if (targetUUID != null) {
			nbt.putUUID("Target", targetUUID);
		}

		writeAdditionalSaveData(nbt);
		return nbt;
	}

	public static StandEffectInstance fromNBT(CompoundTag nbt, Level level) {
		StandEffectType<?> effectType = JojoRegistries.STAND_EFFECTS_REG.get(ResourceLocation.parse(nbt.getString("Type")));
		if (effectType == null) return null;
		StandEffectInstance effect = effectType.create(level);
		effect.tickCount = nbt.getInt("TickCount");
		if (nbt.hasUUID("Target")) {
			effect.targetUUID = nbt.getUUID("Target");
		}

		effect.readAdditionalSaveData(nbt);
		return effect;
	}

	public void writeAdditionalPacketData(FriendlyByteBuf buf, boolean sendingToUser) {}

	public void readAdditionalPacketData(FriendlyByteBuf buf, boolean clientIsUser) {}

	protected void writeAdditionalSaveData(CompoundTag nbt) {}

	protected void readAdditionalSaveData(CompoundTag nbt) {}
}
