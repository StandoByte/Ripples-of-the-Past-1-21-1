package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.mc.entity.projectile.DamagingEntity;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.TrEntityActionPhaseTimePacket;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.powersystem.entityaction.syncdata.SynchedDataExtended;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.mc.EntityResolver;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.AimingEntity;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO (entity action) test the phase lengths stuff (with partial lengths and lengths < 1)
public class EntityActionInstance implements HeldInput, SyncedDataHolderExtended {
	/** Is used in network code, to make sure server and client are on the same page when sending changes to the action's phases from server */
	@ApiStatus.Internal public int id;
	@Nonnull public final EntityActionType ability;
	@ApiStatus.Internal public Object2FloatMap<ActionPhase> phasesLength = new Object2FloatArrayMap<>();
	@ApiStatus.Internal @Nullable public Object2FloatMap<ActionPhase> skippedWindupPhase = null;
	
	@Nullable protected SynchedDataExtended _synchedData;
	protected boolean _emptySynchedData = false;
	
	@Nonnull protected ActionPhase phase;
	protected int curPhaseTick;
	protected float curPhaseLength;
	protected float phasePartialTick;
	protected boolean stoppedHolding = false;
	
	protected LivingEntity performer;
	protected EntityResolver powerUser = new EntityResolver();
	
	@Nullable public ActionTarget standRotationTarget;
	public AimingEntity aimAs = AimingEntity.CAMERA_ENTITY;
	
	/** Stores the target of the punch action, to be able to communicate with other internal systems, like Stand effects */
	@Nullable public ActionTarget punchedTarget;
	
	public float userWalkSpeed = 1;
	
	public EntityActionInstance(EntityActionType ability) {
		this.ability = ability;
	}
	
	/**
	 * After the phase lengths have been initialized properly, this sets up the action's starting phase
	 */
	public void setStartingPhase() {
		for (ActionPhase phase : ActionPhase.values()) {
			if (!phasesLength.containsKey(phase)) {
				phasesLength.put(phase, 0f);
			}
		}
		setPhaseStart(ActionPhase.values()[0]);
	}
	
	public void setSkipWindupPhase(ActionPhase phase, float time) {
		skippedWindupPhase = new Object2FloatArrayMap<>();
		skippedWindupPhase.put(phase, time);
		if (this.phase == phase) {
			float tick = getPhaseTick() + time;
			this.curPhaseTick = (int) tick;
			this.phasePartialTick = tick - this.curPhaseTick;
		}
	}
	
	public void extraClientInput(FriendlyByteBuf input) {}
	

	/**
	 * Is called before the action is synched from the server.
	 */
	@ApiStatus.OverrideOnly
	public void onActionSet(@Nullable EntityActionInstance prevAction) {
		
	}

	@ApiStatus.OverrideOnly
	public void actionTick() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionPerformStart() {
		
	}

	@ApiStatus.OverrideOnly
	public void actionPerformEnd() {
		
	}

	@ApiStatus.OverrideOnly
	public void onSetPhase(ActionPhase newPhase) {
		
	}

	@ApiStatus.OverrideOnly
	public void onActionCleared(@Nullable EntityActionInstance newAction) {
		
	}
	
	@ApiStatus.OverrideOnly
	public void onButtonStopHold() {
		
	}
	
	@ApiStatus.OverrideOnly
	public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
		return phase == ActionPhase.RECOVERY;
	}
	
	
	@ApiStatus.OverrideOnly
	public void toBuf(FriendlyByteBuf buf) {}

	@ApiStatus.OverrideOnly
	public void fromBuf(FriendlyByteBuf buf) {}
	

	@Nullable
	@ApiStatus.NonExtendable
	public SynchedDataExtended getSynchedData(boolean clientSide) {
		if (_synchedData == null && !_emptySynchedData) {
			SynchedEntityData.Builder builder = new SynchedEntityData.Builder(this);
			defineSynchedData(builder);
			boolean isEmpty = builder.itemsById.length == 0;
			if (isEmpty) {
				_emptySynchedData = true;
			}
			else {
				_synchedData = new SynchedDataExtended(builder, clientSide);
			}
		}
		return _synchedData;
	}
	
	public <T> T getSynchedData(EntityDataAccessor<T> key) {
		return getSynchedData(level().isClientSide()).get(key);
	}

	public <T> void setSynchedData(EntityDataAccessor<T> key, T value) {
		setSynchedData(key, value, false);
	}

	public <T> void setSynchedData(EntityDataAccessor<T> key, T value, boolean forceUpdate) {
		getSynchedData(level().isClientSide()).set(key, value, forceUpdate);
	}

	@ApiStatus.OverrideOnly
	public void defineSynchedData(SynchedEntityData.Builder builder) {}
	
	@Override
	public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataKey) {
		onSyncedDataUpdated(dataKey);
	}
	
	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> dataKey) {}

	@Override
	public void onSyncedDataUpdated(List<SynchedEntityData.DataValue<?>> newData) {}
	
	
	// Some helper methods to write less boilerplate in Stand abilities
	
	public void setStandOffset(double left, double front, StandOffsetFromUser.Rotations rotations, boolean changeOnlyIfIdle) {
		if (performer instanceof StandEntity standEntity) {
			Vec3 relativeOffset = new Vec3(left, standEntity.Y_OFFSET, front);
			_setStandOffset(standEntity, relativeOffset, rotations, changeOnlyIfIdle);
		}
	}
	
	public void setStandOffset(Vec3 relativeOffset, StandOffsetFromUser.Rotations rotations, boolean changeOnlyIfIdle) {
		if (performer instanceof StandEntity standEntity) {
			_setStandOffset(standEntity, relativeOffset, rotations, changeOnlyIfIdle);
		}
	}
	
	public void _setStandOffset(StandEntity standEntity, Vec3 relativeOffset, StandOffsetFromUser.Rotations rotations, boolean changeOnlyIfIdle) {
		LivingEntity user = standEntity.getUser();
		if (user != null && (!changeOnlyIfIdle || standEntity.offsetFromUser.isIdle())) {
			standEntity.offsetFromUser.setOffset(relativeOffset, rotations);
			standEntity.offsetFromUser.standAbility = this.ability;
		}
	}
	
	public boolean standEntityAttack(StandEntity stand, Entity target, DamageSource dmgSource, float dmgAmount) {
		ServerLevel level = (ServerLevel) target.level();
		boolean hurt = target.hurt(dmgSource, dmgAmount);
		if (hurt) {
			if (target instanceof LivingEntity targetLiving) {
				LivingEntity user = stand.getUser();
				if (user != null) {
					LivingEntity aggroTo = stand.isFollowingUser() || targetLiving.hasLineOfSight(user) ? user : 
						StandUtil.isEntityStandUser(targetLiving) ? stand : null;
					if (aggroTo != null && aggroTo != dmgSource.getEntity()) {
						Brain<?> brain = targetLiving.getBrain();
						Optional<LivingEntity> brainAttackTarget = brain.getMemoryInternal(MemoryModuleType.ATTACK_TARGET);
						if (brainAttackTarget != null && brainAttackTarget.filter(t -> t == dmgSource.getEntity()).isPresent()) {
							brain.setMemory(MemoryModuleType.ATTACK_TARGET, aggroTo);
						}
					}
				}
			}
            EnchantmentHelper.doPostAttackEffects(level, target, dmgSource);
		}
		return hurt;
	}
	
	public void keepStandAimedAtTarget() {
		Level level = level();
		if (!level.isClientSide()) {
			ActionTarget aimTarget = LivingComponentAction.getAim(performer).getTarget();
			if (!aimTarget.isEmpty(level)) {
				standRotationTarget = aimTarget;
			}
		}
	}
	
	public final float calcFullTicks(ActionPhase targetPhase, float targetPhaseTick) {
		float sum = 0;
		for (ActionPhase phase : ActionPhase.values()) {
			float length = phasesLength.getFloat(phase);
			if (phase == targetPhase) {
				length = Math.min(length, targetPhaseTick);
			}
			sum += length;
			if (phase == targetPhase) break;
		}
		return sum;
	}
	
	/**
	 * A function to time the punch swing sounds a few ticks before the actual punch impact
	 */
	public final boolean soundTiming(ActionPhase targetPhase, float targetPhaseTick, int soundOffset) {
		float ticksPassed = getFullTicksPassed();
		int ticksDiff = (int) (ticksPassed - calcFullTicks(targetPhase, targetPhaseTick));
		return ticksDiff == soundOffset
				|| soundOffset < 0 && soundOffset < ticksDiff && (int) ticksPassed == 0
				/*|| soundOffset > 0 && ... */;
	}
	
	public final boolean isUserCreative() {
		LivingEntity user = getPowerUser();
		return user instanceof Player player && player.getAbilities().instabuild;
	}
	
	public void tossStandHeldItems(EquipmentSlot... slots) {
		Level level = level();
		if (!level.isClientSide() && performer instanceof StandEntity stand) {
			LivingEntity user = powerUser.getEntityLiving(level);
			Vec3 tossVec = user != null ? user.position().subtract(stand.getEyePosition()) : stand.getLookAngle();
			for (EquipmentSlot slot : slots) {
				stand.tossItem(slot, tossVec);
			}
		}
	}
	
	public void addProjectileWithStandStats(DamagingEntity projectile) {
		Level level = level();
		if (!level.isClientSide() && !projectile.isAddedToLevel() && performer instanceof StandEntity stand) {
			projectile.setDamageFactor(projectile.getDamageFactor() * (float) stand.getAttackDamage() / 8);
			projectile.setSpeedFactor(projectile.getSpeedFactor() * stand.getAttackSpeed() / 8);
			level.addFreshEntity(projectile);
		}
	}
	
	protected Level level() {
		return performer.level();
	}
	
	
	
	
	
	@ApiStatus.NonExtendable
	public float getPhaseTick() {
		return curPhaseTick + phasePartialTick;
	}

	@ApiStatus.NonExtendable
	public float getPhaseTicksLeft() {
		return curPhaseLength - getPhaseTick();
	}

	@ApiStatus.NonExtendable
	public float getCurPhaseLength() {
		return curPhaseLength;
	}

	@ApiStatus.NonExtendable
	public ActionPhase getPhase() {
		return phase;
	}

	@ApiStatus.NonExtendable
	public float getPhaseRatio() {
		if (curPhaseLength == 0) throw new IllegalStateException();
		return Math.min(getPhaseTick() / curPhaseLength, 1);
	}
	
	public float getFullTicksPassed() {
		return calcFullTicks(this.phase, this.getPhaseTick());
	}
	
	/* When a click and a hold ability share the same key, the code (LivingComponentAction#skipWindupTime(EntityActionInstance, float))
	 * adjusts for the time it took to distinguish between the two by skipping a little bit of the windup phase.
	 * For the sake of keeping animations smooth and abilities consistent, this is handled differently for actual action phases and animations.
	 * 
	 * For the actual phase timer, the real phase length is preserved, and the skipped time is added to the timer
	 *   (the windup phase will start at tick 4/20).
	 * In the animations, the skipped time is deducted from the phase length
	 *   (the windup animation will start at tick 0/16).
	 */
	
	@ApiStatus.NonExtendable
	public float getAnimPhaseTick(float partialTick) {
		float phaseTick = curPhaseTick + phasePartialTick;
		if (skippedWindupPhase != null) {
			phaseTick -= skippedWindupPhase.getOrDefault(this.phase, 0);
		}
		return phaseTick + partialTick;
	}
	
	@ApiStatus.NonExtendable
	public float getAnimPhaseLength() {
		float phaseLength = curPhaseLength;
		if (skippedWindupPhase != null) {
			phaseLength -= skippedWindupPhase.getOrDefault(this.phase, 0);
		}
		return phaseLength;
	}

	@ApiStatus.NonExtendable
	public float getAnimPhaseRatio(float partialTick) {
		if (curPhaseLength == 0) throw new IllegalStateException();
		float phaseTick = getAnimPhaseTick(partialTick);
		float phaseLength = getAnimPhaseLength();
		return phaseTick / phaseLength;
	}
	
	public float getAnimFullTicksPassed(float partialTick) {
		float ticks = getFullTicksPassed();
		if (skippedWindupPhase != null) {
			for (ActionPhase phase : ActionPhase.values()) {
				float skipped = skippedWindupPhase.getFloat(phase);
				ticks -= skipped;
				if (phase == getPhase()) break;
			}
		}
		return ticks + partialTick;
	}
	
	public void extractAnim(EntityActionRenderState renderState, float partialTick) {
		renderState.animId = getEntityAnim();
		renderState.time = getAnimFullTicksPassed(partialTick);
		renderState.actionPhase = getPhase();
		renderState.phaseTime = getAnimPhaseTick(partialTick);
		renderState.phaseCompletion = getAnimPhaseRatio(partialTick);
	}

	public ActionAnimIdentifier getEntityAnim() {
		return ability.getEntityAnim(this);
	}
	

	@ApiStatus.NonExtendable
	public void forceStop() {
		setPhaseStart(null);
	}

	@ApiStatus.NonExtendable
	public boolean isOver() {
		return phase == null;
	}
	
	
	public LivingEntity getPerformer() {
		return performer;
	}
	
	public LivingEntity getPowerUser() {
		if (performer != null) {
			return powerUser.getEntityLiving(performer.level());
		}
		return null;
	}
	
	
	

	// TODO (!) (entity action) test partial tick for consecutive actions
	@ApiStatus.Internal
	public void setPartialTick(float partialTick) {
		if (partialTick >= 1) throw new IllegalArgumentException();
		this.phasePartialTick = partialTick;
	}
	
	public void setPhaseStart(ActionPhase phase) {
		setPhase(phase, 0);
	}

	public void setPhase(ActionPhase phase, int tick) {
		if (phase == null) {
			this.phase = null;
			return;
		}
		
		float prevPhaseTick = getPhaseTick();
		float prevTickLength = this.curPhaseLength;
		
		if (this.performer != null && this.phase != phase) {
			onSetPhase(phase);
		}
		this.phase = phase;
		this.curPhaseTick = tick;
		this.curPhaseLength = phase != null ? phasesLength.getFloat(phase) : -1;
		
		this.phasePartialTick = Mth.clamp(prevPhaseTick - prevTickLength, 0, 0.9999f);
		
		checkNextPhase();
	}
	
	// XXX only sync actual phase changes
	public void syncPhaseChanges() {
		if (performer != null && !performer.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(performer, new TrEntityActionPhaseTimePacket(performer.getId(), 
					id, phasesLength, phase, curPhaseTick));
		}
	}


	@ApiStatus.Internal
	public void _onActionStarted(@Nullable EntityActionInstance prevAction) {
		onActionSet(prevAction);
		onSetPhase(phase);
	}

	@ApiStatus.Internal
	public void _beforeActionRemoved(@Nullable EntityActionInstance newAction) {
		onActionCleared(newAction);
		this.phase = null;
	}
	
	@ApiStatus.Internal
	public void _tickAction() {
		if (!isOver()) {
			_onTick();
			_incPhaseTick();
			checkNextPhase();
		}
	}

	@ApiStatus.Internal
	protected void _incPhaseTick() {
		++curPhaseTick;
	}
	
	@ApiStatus.Internal
	protected void _onTick() {
		actionTick();
		if (phase == ActionPhase.PERFORM) {
			if (getPhaseTick() < 1) {
				actionPerformStart();
			}
			if (getPhaseTick() + 1 >= curPhaseLength) {
				actionPerformEnd();
			}
		}
	}
	
	@ApiStatus.Internal
	protected void checkNextPhase() {
		if (!isOver() && getPhaseTick() >= curPhaseLength) {
			// tick a skipped non-zero phase
			if (curPhaseTick == 0 && curPhaseLength > 0 && curPhaseLength <= 1) {
				_onTick();
			}
			
			int ordinal = phase.ordinal() + 1;
			ActionPhase nextPhase = ordinal < ActionPhase.values().length ? ActionPhase.values()[ordinal] : null;
			setPhaseStart(nextPhase);
		}
	}

	
	@Override
	@ApiStatus.Internal
	public void onKeyRelease(LivingEntity user) {
		if (!this.isOver()) {
			onButtonStopHold();
		}
	}
	
	
	public static void encode(RegistryFriendlyByteBuf buffer, EntityActionInstance action) {
		ActionPhase actionPhase = action != null ? action.phase : null;
		boolean valid = action != null && !action.isOver();
		buffer.writeBoolean(valid);
		if (valid) {
			action.ability.encodeAbility(action.getPowerUser(), buffer);

			buffer.writeVarInt(action.id);
			for (ActionPhase phase : ActionPhase.values()) {
				buffer.writeFloat(action.phasesLength.getFloat(phase));
			}
			NetworkUtil.writeOptionally(action.skippedWindupPhase, buffer, (buf, map) -> {
				buf.writeVarInt(map.size());
				for (var entry : map.object2FloatEntrySet()) {
					buf.writeEnum(entry.getKey());
					buf.writeFloat(entry.getFloatValue());
				}
			});
			buffer.writeVarInt(actionPhase.ordinal());
			buffer.writeVarInt(action.curPhaseTick);
			buffer.writeFloat(action.phasePartialTick);
			buffer.writeFloat(action.curPhaseLength);
			action.powerUser.writeNetwork(buffer);
			NetworkUtil.writeOptionally(action.standRotationTarget, buffer, ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID);
			action.toBuf(buffer);
		}
	}

	public static EntityActionInstance decode(Level level, FriendlyByteBuf buffer) {
		boolean valid = buffer.readBoolean();
		if (valid) {
			EntityActionInstance action = EntityActionType.decodeAbilityAction(level, buffer);
			if (action != null) {
				action.id = buffer.readVarInt();
				for (ActionPhase phase : ActionPhase.values()) {
					action.phasesLength.put(phase, buffer.readFloat());
				}
				action.skippedWindupPhase = NetworkUtil.readOptional(buffer, (buf) -> {
					Object2FloatArrayMap<ActionPhase> map = new Object2FloatArrayMap<>();
					int size = buf.readVarInt();
					for (int i = 0; i < size; i++) {
						map.put(buf.readEnum(ActionPhase.class), buf.readFloat());
					}
					return map;
				}).orElse(null);
				action.phase = ActionPhase.values()[buffer.readVarInt()];
				action.curPhaseTick = buffer.readVarInt();
				action.phasePartialTick = buffer.readFloat();
				action.curPhaseLength = buffer.readFloat();
				action.powerUser.readNetwork(buffer);
				action.standRotationTarget = NetworkUtil.readOptional(buffer, ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID).orElse(null);
				action.fromBuf(buffer);
				return action;
			}
		}
		
		return null;
	}
	
}
