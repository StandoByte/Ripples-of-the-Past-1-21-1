package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.entityanim.pose.EntityKeepAnimPose;
import com.github.standobyte.jojo.core.packet.fromserver.TrSetStandEntityPacket;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.mc.entity.projectile.DamagingEntity;
import com.github.standobyte.jojo.mc.entity.util.EntityStandVisibility;
import com.github.standobyte.jojo.mc.entity.util.EntityWithStandSkin;
import com.github.standobyte.jojo.mc.entity.util.HandItemsAsInventory;
import com.github.standobyte.jojo.mc.entity.util.LivingReactToNewAction;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.mechanics.externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.MathUtil.AABBDist;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;
import com.github.standobyte.jojo.util.damage.StandLinkDamageSource;
import com.github.standobyte.jojo.util.java.Lerp;
import com.github.standobyte.jojo.util.mc.AttributeUtil;
import com.github.standobyte.jojo.util.mc.PrevRotations;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityUnsummonAction;
import com.github.standobyte.jojoimpl.stands._helditems.StandHandsContainerMenu;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandEntity extends LivingEntity implements SummonedStand, IEntityWithComplexSpawn, LivingReactToNewAction, EntityStandVisibility, EntityWithStandSkin {
	protected ResourceLocation standId;
	protected static final EntityDataAccessor<Byte> STAND_FLAGS = SynchedEntityData.defineId(StandEntity.class, EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Integer> USER_ID = SynchedEntityData.defineId(StandEntity.class, EntityDataSerializers.INT);
	protected WeakReference<LivingEntity> userRef = new WeakReference<>(null);
	protected StandPower userPower;
	protected final LivingComponentAction standAction;
	
	protected static final EntityDataAccessor<Float> FINISHER_VALUE = SynchedEntityData.defineId(StandEntity.class, EntityDataSerializers.FLOAT);
	
	public double Y_OFFSET = 0.2;
	public StandOffsetFromUser offsetFromUser;
    public double rangeEfficiency = 1;
    public double staminaCondition = 1;
    public Lerp.FloatValue modelAlpha = new Lerp.FloatValue(1);
	
	public ClientStandEntityStuff clientStuff;

	public StandEntity(EntityType<? extends StandEntity> type, Level level) {
		super(type, level);
		this.standAction = LivingComponentAction.getComponent(this);
		this.offsetFromUser = StandOffsetFromUser.createDefault(this);
		if (level.isClientSide()) {
			this.clientStuff = new ClientStandEntityStuff();
			((EntityKeepAnimPose) this).jojo_ripples$setKeepModelPose(true);
		}
	}
	
	public StandEntity withStandType(StandType standType) {
		if (isAddedToLevel()) throw new IllegalStateException();
		this.standId = standType.getId();
		initStandStatsValues(standType.getStandStats());
		return this;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(USER_ID, -1);
		builder.define(STAND_FLAGS, defaultStandFlags());
		builder.define(FINISHER_VALUE, 0f);
	}
	
	@Override
	public void onAddedToLevel() {
		super.onAddedToLevel();
		if (standHasNoGravity) {
			setNoGravity(true);
		}
		if (standCanHaveNoPhysics) {
			noPhysics = true;
		}
		
		openStandHandsContainer();
	}
	

	public PrevRotations rotO = new PrevRotations();
	@Override
	public void tick() {
		fallDistance = 0;
		modelAlpha.set(1, true);
		rotO.rememberAngles(this);
		LivingEntity user = getUser();
		Level level = level();
		if (!level.isClientSide()) {
			if (requiresUser() && (user == null || user.isRemoved())) {
				this.remove(user != null ? user.getRemovalReason() : RemovalReason.DISCARDED);
				return;
			}
		}
		
		updateStandStatAttributes(this, user);
		
		super.tick();
		
		if (user != null) {
			UtilFunctions.wrapYRotationAngles(user);
			updatePosition(user);
			if (!level.isClientSide()) {
				tickHealth(user);
			}
			updateUserOffset(user);
		}
		this.xRotO = rotO.xRot;
		this.yRotO = rotO.yRot;
		this.yBodyRotO = rotO.yBodyRot;
		
		yHeadRot = getYRot();
		yHeadRotO = yRotO;

        updateStrengthMultipliers();
		tickFinisherMeter();
	}
	
	@Override
	public void aiStep() {
		super.aiStep();
		pickUpItemEntities();
	}
	
	@Override
	public void remove(Entity.RemovalReason reason) {
		if (reason.shouldDestroy() && level() instanceof ServerLevel) {
			dropEquipment(/*level*/);
		}
		super.remove(reason);
	}

	
	@Override
	public void setUserAndPower(LivingEntity user, StandPower power) {
		if (!level().isClientSide()) {
			entityData.set(USER_ID, user.getId());
		}
		this.userPower = power;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> dataParameter) {
		super.onSyncedDataUpdated(dataParameter);
		if (STAND_FLAGS.equals(dataParameter)) {
			noPhysics = getStandFlag(StandFlag.NO_PHYSICS);
		}
		else if (USER_ID.equals(dataParameter)) {
			updateUserFromNetwork(entityData.get(USER_ID));
		}
	}
	
	@Override
	public void tickStand(LivingEntity user, StandPower userStand) {
		if (!user.level().isClientSide() && this.isRemoved()) {
			userStand.setSummonedStand(null);
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrSetStandEntityPacket(user.getId(), 0));
		}
	}
	
	@Override
	public StandEntity getStandEntity() {
		return this;
	}
	
	/**
	 * Careful - the user's entity might not always be loaded on client in case of long-ranged Stands.
	 */
	@Nullable
	public LivingEntity getUser() {
		if (hasUser()) {
			LivingEntity user = userRef.get();
			if (user == null) {
				user = lookupUser(entityData.get(USER_ID));
				if (user != null) {
					setUserRef(user);
				}
			}
			return user;
		}
		return null;
	}

	@Nullable
	public StandPower getUserPower() {
		if (userPower == null && hasUser()) {
			LivingEntity user = getUser();
			if (user != null) {
				userPower = StandPower.get(user);
			}
		}
		return userPower;
	}

	protected final boolean hasUser() {
		return entityData.get(USER_ID) >= 0;
	}
	
	// XXX left-side stand pos config
	protected void updateUserFromNetwork(int userId) {
		LivingEntity user = lookupUser(userId);
		setUserRef(user);
		if (user != null) {
//			if (user instanceof Player) {
//				playerSettings = PlayerClientBroadcastedSettings.getPlayerSettings((Player) user);
//			}
			if (level().isClientSide()) {
				StandPower standPower = StandPower.get(user);
				if (standPower != null && standPower.getSummonedStand() != this) {
					standPower.setSummonedStand(this);
				}
			}
		}
	}

	@Nullable
	protected LivingEntity lookupUser(int userId) {
		Entity user = level().getEntity(userId);
		return user instanceof LivingEntity living ? living : null;
	}
	
	protected void setUserRef(LivingEntity userEntity) {
		this.userRef = new WeakReference<>(userEntity);
	}
	
	
	public void updatePosition(LivingEntity user) {
		if (isFollowingUser()) {
			if (user != null) {
				Vec3 pos = offsetFromUser.getPosition(user);
				setPos(pos.x, pos.y, pos.z);
				copyStandUserRotation(user);
			}
			lookAtCurTarget(rotO);
		}
		else if (isManuallyControlled()) {
			moveStandManualControl();
		}
		if (isBeingRetracted() && user != null) {
			if (!isCloseToUser()) {
				Vec3 targetPos = offsetFromUser.getPosition(user);
				Vec3 movementVec = targetPos.subtract(position());
				setDeltaMovement(movementVec.normalize().scale(getAttributeValue(Attributes.MOVEMENT_SPEED)));
			}
			else {
				setDeltaMovement(Vec3.ZERO);
				setStandFlag(StandFlag.BEING_RETRACTED, false);
			}
		}
	}
	
	public void copyStandUserRotation(LivingEntity user) {
		offsetFromUser.copyRotation(user, level().isClientSide());
	}
	
	public boolean lookAtCurTarget(PrevRotations rotO) {
		ActionTarget lookTarget;
		EntityActionInstance curAction = standAction.getAction();
		boolean fullyRotateBody = curAction != null && LivingComponentGrab.getEntityGrabbedBy(this) == null;
		if (curAction != null) {
			lookTarget = curAction.standRotationTarget;
			if (lookTarget == null) {
				lookTarget = ActionTarget.EMPTY;
			}
			else if (lookTarget.isEmpty(level())) {
				curAction.standRotationTarget = null;
				lookTarget = ActionTarget.EMPTY;
			}
		}
		else {
			ActionTarget crosshairTarget = standAction.entityAim.getTarget();
			if (crosshairTarget.getType() == TargetType.ENTITY) {
				lookTarget = crosshairTarget;
			}
			else {
				lookTarget = ActionTarget.EMPTY;
			}
		}
		
		Vec3 targetPos = switch (lookTarget.getType()) {
			case ENTITY -> {
				Entity targetEntity = lookTarget.getEntity();
                if (targetEntity != null){
                    // TODO (stand aiming) look closer to where the user is looking (legs/head aiming)
                    double y = targetEntity instanceof LivingEntity ?
                            targetEntity.getEyeY() :
                            (targetEntity.getBoundingBox().minY + targetEntity.getBoundingBox().maxY) / 2.0;
                    yield new Vec3(targetEntity.getX(), y, targetEntity.getZ());
                }
				yield null;
			}
			case BLOCK -> {
				yield Vec3.atCenterOf(lookTarget.getBlockPos());
			}
			default -> null;
		};
		
		if (targetPos != null) {
			Vec2 rotations = MathUtil.lookAnglesTowards(targetPos, this, EntityAnchorArgument.Anchor.EYES);
			if (fullyRotateBody) {
				this.setXRot(rotations.x);
				this.setYRot(rotations.y);
				
				this.setYHeadRot(this.getYRot());
				this.setYBodyRot(this.getYRot());
			}
			else {
				float maxHeadYRot = 75;
				float f2 = Mth.wrapDegrees(yBodyRot - rotations.y);
				if (Math.abs(f2) < maxHeadYRot) {
					this.setXRot(rotations.x);
					this.setYRot(rotations.y);
					this.setYHeadRot(this.getYRot());
				}
			}
//			this.xRotO = rotO.xRot;
//			this.yRotO = rotO.yRot;
//			this.yHeadRotO = rotO.yHeadRot;
//			this.yBodyRotO = rotO.yBodyRot;
			return true;
		}
		return false;
	}
	
	protected Vec3 _offsetFromUserVec;
	protected Vec3 _manualControlInput = Vec3.ZERO;
	protected void updateUserOffset(LivingEntity user) {
		if (user != null) {
			this._offsetFromUserVec = this.position().subtract(user.position());
		}
	}
	
	public void manualControlInput(Vec3 motionInput) {
		this._manualControlInput = motionInput;
	}
	
	protected void moveStandManualControl() {
		LivingEntity user = getUser();
		if (user != null && isControlledByLocalInstance()) {
			if (_offsetFromUserVec == null) {
				updateUserOffset(user);
			}
			
			if (_offsetFromUserVec != null) {
				_offsetFromUserVec = _offsetFromUserVec.add(_manualControlInput);
				_manualControlInput = Vec3.ZERO;
				
				Vec3 userPos = user.position();
				Vec3 newPos = userPos.add(_offsetFromUserVec);
				Vec3 move = newPos.subtract(this.position());
				move(MoverType.SELF, move);
			}
			else {
				move(MoverType.SELF, _manualControlInput);
				_manualControlInput = Vec3.ZERO;
			}
		}
	}

	@Override
	public void move(MoverType type, Vec3 vec) {
		super.move(type, vec);
		
		LivingEntity user = getUser();
		Level level = this.level();
		if (user != null && user.level() == level) {
			AABBDist bbDistance = MathUtil.getAABBDistanceDetailed(this.getBoundingBox(), user.getBoundingBox());
			double distance = bbDistance.distance();
			double range = getMaxRange();
			if (distance > range) {
				Vec3 standPos = bbDistance.posBB1();
				Vec3 userPos = bbDistance.posBB2();
				Vec3 vecToUser = userPos.subtract(standPos).scale(1 - range / distance);
				moveWithoutCollision(vecToUser);
			}
			if (!vec.equals(Vec3.ZERO)) {
				updateUserOffset(user);
			}
		}
	}

	protected void moveWithoutCollision(Vec3 moveVec) {
		AABB bb = getBoundingBox().move(moveVec);
		setBoundingBox(bb);
		setPosRaw((bb.minX + bb.maxX) / 2, bb.minY, (bb.minZ + bb.maxZ) / 2);
	}

	@Override
	public boolean isControlledByLocalInstance() {
		if (isManuallyControlled()) {
			Entity user = getUser();
			if (user instanceof Player player) {
				return player.isLocalPlayer();
			}
		}
		return isEffectiveAi();
	}

	
	protected void setStandFlag(StandFlag flag, boolean value) {
		byte i = entityData.get(STAND_FLAGS);
		if (value) {
			i |= flag.bit;
		} else {
			i &= ~flag.bit;
		}
		entityData.set(STAND_FLAGS, i);
	}
	
	protected byte defaultStandFlags() {
		byte i = 0;
		for (StandFlag flag : StandFlag.values()) {
			if (flag.defaultValue) {
				i |= flag.bit;
			}
		}
		
		if (standCanHaveNoPhysics) {
			i |= StandFlag.NO_PHYSICS.bit;
		} else {
			i &= ~StandFlag.NO_PHYSICS.bit;
		}
		
		return i;
	}

	public boolean getStandFlag(StandFlag flag) {
		return (entityData.get(STAND_FLAGS) & flag.bit) != 0;
	}

	public static enum StandFlag {
		MANUAL_CONTROL(false),
		CAN_FOLLOW_USER(true),
		BEING_RETRACTED(false),
		NO_PHYSICS(true);

		public final byte bit;
		public final boolean defaultValue;
		private StandFlag(boolean defaultValue) {
			this.bit = (byte) (1 << ordinal());
			this.defaultValue = defaultValue;
		}
	}
	
	
	public boolean isFollowingUser() {
		return !isManuallyControlled() && followingUserIsEnabled() && !isBeingRetracted();
	}
	
	public boolean isManuallyControlled() {
		// makes it smoother if you move as soon as you enter manual control, otherwise there is a little stumble
		if (level().isClientSide() && getUser() == ClientProxy.getClientPlayer()) {
			ClientEntityController ctrl = ClientEntityController.getInstance();
			return ctrl != null && ctrl.entity == this;
		}
		return getStandFlag(StandFlag.MANUAL_CONTROL);
	}
	
	public void setManuallyControlled(boolean value) {
		setStandFlag(StandFlag.MANUAL_CONTROL, value);
		if (level().isClientSide()) {
			setDeltaMovement(Vec3.ZERO);
		}

		if (!value && followingUserIsEnabled()) {
			retract();
		}
		else {
			setStandFlag(StandFlag.BEING_RETRACTED, false);
		}
		
		updateNoPhysics();
	}
	
	public void setCanFollowUser(boolean enabled) {
		setStandFlag(StandFlag.CAN_FOLLOW_USER, enabled);
	}
	
	public boolean followingUserIsEnabled() {
		return getStandFlag(StandFlag.CAN_FOLLOW_USER);
	}
	
	public boolean isBeingRetracted() {
		return getStandFlag(StandFlag.BEING_RETRACTED);
	}
	
	public void retract() {
		LivingEntity user = getUser();
		if (user != null) {
			setStandFlag(StandFlag.BEING_RETRACTED, true);
		}
	}

	public void retractAndUnsummon() {
		LivingEntity user = getUser();
		if (user != null) {
			if (!isFollowingUser()) {
				setStandFlag(StandFlag.BEING_RETRACTED, true);
			}
			startStandUnsummon();
		}
	}
	
	public boolean isCloseToUser() {
		LivingEntity user = getUser();
		return user != null ? distanceToSqr(user) < 4 : false;
	}
	
	public void onUnsummonUserInput() {
		// TODO on unsummon input, only cancel current stand action
		if (!this.isBeingRetracted()) {
			this.retractAndUnsummon();
		}
		else if (this.isManuallyControlled()) {
			this.stopRetraction();
		}
	}

	public void stopRetraction() {
		setStandFlag(StandFlag.BEING_RETRACTED, false);
		
		EntityActionInstance curAction = getCurStandAction();
		if (curAction != null && curAction.ability == ModSpecialActions.STAND_UNSUMMON.get()) {
			standAction.setAction(null, SyncType.TRACKING_AND_SELF);
		}
	}

	protected void startStandUnsummon() {
		if (!level().isClientSide()) {
			var unsummonAction = new StandEntityUnsummonAction.StandUnsummonInstance();
			standAction.setAction(unsummonAction, getUser(), SyncType.TRACKING_AND_SELF);
		}
	}

	public void updateNoPhysics() {
		setNoPhysics(shouldHaveNoPhysics());
	}

	protected boolean shouldHaveNoPhysics() {
		return standCanHaveNoPhysics && !isManuallyControlled() && followingUserIsEnabled();
	}

	public void setNoPhysics(boolean noPhysics) {
		if (noPhysics || standCanHaveNoPhysics) {
			setStandFlag(StandFlag.NO_PHYSICS, noPhysics);
		}
	}
	
	
	public void multiplyTranslucency(float multiplier) {
		modelAlpha.set(modelAlpha.get() * multiplier, false);
	}
	
	
	@Nullable
	public EntityActionInstance getCurStandAction() {
		return standAction.getAction();
	}
	
	@Nonnull
	public LivingComponentAction getStandActionComponent() {
		return standAction;
	}
	
	@Override
	public boolean onActionSet(@Nullable EntityActionInstance action) {
		if (action != null) {
			setNoPhysics(false);
		}
		else {
			updateNoPhysics();
			offsetFromUser.resetToIdle();
		}
		return false;
	}
	// TODO (entity action 2) sync on load
	// TODO (entity action 2) sync already existing action with tracking
	
	
	protected static final double DEFAULT_ATTACK_RANGE = 4;
	public static AttributeSupplier.Builder createAttributes() {
		return LivingEntity.createLivingAttributes()
			.add(Attributes.ATTACK_DAMAGE, 8)
			.add(Attributes.MOVEMENT_SPEED, 0.5)
			.add(Attributes.ATTACK_SPEED, 8)
			.add(Attributes.BLOCK_INTERACTION_RANGE, DEFAULT_ATTACK_RANGE)
			.add(Attributes.ENTITY_INTERACTION_RANGE, DEFAULT_ATTACK_RANGE)
			.add(ModEntityAttributes.STAND_EFFECTIVE_RANGE, 2)
			.add(ModEntityAttributes.STAND_MAX_RANGE, 4)
			.add(ModEntityAttributes.STAND_DURABILITY, 8)
			.add(ModEntityAttributes.STAND_PRECISION, 8)
			.add(Attributes.LUCK)
			.add(Attributes.BLOCK_BREAK_SPEED)
			.add(Attributes.SUBMERGED_MINING_SPEED)
			.add(Attributes.SNEAKING_SPEED)
			.add(Attributes.MINING_EFFICIENCY)
			.add(Attributes.SWEEPING_DAMAGE_RATIO);
	}

	public void initStandStatsValues(StandStats stats) {
		getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(stats.power());
		getAttribute(Attributes.ATTACK_SPEED).setBaseValue(stats.speed());
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(StandStatFormulas.getMovementSpeed(stats.speed()));
		getAttribute(ModEntityAttributes.STAND_EFFECTIVE_RANGE).setBaseValue(stats.rangeEffective());
		getAttribute(ModEntityAttributes.STAND_MAX_RANGE).setBaseValue(stats.rangeMax());
		getAttribute(ModEntityAttributes.STAND_DURABILITY).setBaseValue(stats.durability());
		getAttribute(ModEntityAttributes.STAND_PRECISION).setBaseValue(stats.precision());
	}
	
	public static void updateStandStatAttributes(LivingEntity stand, @Nullable LivingEntity user) {
		if (user != null) {
			AttributeMap standAttributes = stand.getAttributes();
			AttributeMap userAttributes = user.getAttributes();
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_STRENGTH)) {
				double strength = userAttributes.getValue(ModEntityAttributes.STAND_STRENGTH);
				AttributeUtil.setBaseValue(standAttributes, Attributes.ATTACK_DAMAGE, strength);
			}
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_SPEED)) {
				double speed = userAttributes.getValue(ModEntityAttributes.STAND_SPEED);
				AttributeUtil.setBaseValue(standAttributes, Attributes.ATTACK_SPEED, speed);
				AttributeUtil.setBaseValue(standAttributes, Attributes.MOVEMENT_SPEED, StandStatFormulas.getMovementSpeed(speed));
			}
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_EFFECTIVE_RANGE)) {
				double effectiveRange = userAttributes.getValue(ModEntityAttributes.STAND_EFFECTIVE_RANGE);
				AttributeUtil.setBaseValue(standAttributes, ModEntityAttributes.STAND_EFFECTIVE_RANGE, effectiveRange);
			}
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_MAX_RANGE)) {
				double maxRange = userAttributes.getValue(ModEntityAttributes.STAND_MAX_RANGE);
				AttributeUtil.setBaseValue(standAttributes, ModEntityAttributes.STAND_MAX_RANGE, maxRange);
			}
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_DURABILITY)) {
				double durability = userAttributes.getValue(ModEntityAttributes.STAND_DURABILITY);
				AttributeUtil.setBaseValue(standAttributes, ModEntityAttributes.STAND_DURABILITY, durability);
			}
			if (userAttributes.hasAttribute(ModEntityAttributes.STAND_PRECISION)) {
				double precision = userAttributes.getValue(ModEntityAttributes.STAND_PRECISION);
				AttributeUtil.setBaseValue(standAttributes, ModEntityAttributes.STAND_PRECISION, precision);
			}
		}
	}

	public double getAttackDamage() {
		double damage = getAttributeValue(Attributes.ATTACK_DAMAGE);
		return damage * getStandEfficiency();
	}

	public double getAttackSpeed() {
		double speed = getAttributeValue(Attributes.ATTACK_SPEED);
		return speed * getStandEfficiency();
	}

	public double getAttackKnockback() {
		double damage = getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		return damage * getStandEfficiency();
	}

	public double getDurability() {
		double durability = getAttributeValue(ModEntityAttributes.STAND_DURABILITY);
//		if (ModPowers.VAMPIRISM.get().isHighOnBlood(getUser())) {
//			durability *= 2;
//		}
		return durability * getStandEfficiency();
	}

	public double getPrecision() {
		double precision = getAttributeValue(ModEntityAttributes.STAND_PRECISION);
		return precision * getStandEfficiency();
	}
	
	public double getEffectiveRange() {
		return getAttributeValue(ModEntityAttributes.STAND_EFFECTIVE_RANGE);
	}
	
	public double getMaxRange() {
		return getAttributeValue(ModEntityAttributes.STAND_MAX_RANGE);
	}
	
	public double getStandEfficiency() {
		return rangeEfficiency * staminaCondition;
	}

	public void updateStrengthMultipliers() {
		LivingEntity user = getUser();

		rangeEfficiency = user != null ? StandStatFormulas.rangeStrengthFactor(getEffectiveRange(), getMaxRange(), 
				MathUtil.getAABBDistance(this.getBoundingBox(), user.getBoundingBox())) : 1;

		if (user != null && userPower != null) {
			staminaCondition = StandUtil.staminaCondition(userPower);
		}
	}
	
	
	public boolean isArmsOnlyMode() {
		return false;
	}

	
	@Override
	public ResourceLocation getStandType() {
		return standId;
	}

	protected Optional<ResourceLocation> standSkin = Optional.empty();
	@Override
	public void setSelectedSkin(Optional<ResourceLocation> standSkin) {
		this.standSkin = standSkin;
	}
	
	@Override
	public Optional<ResourceLocation> getStandSkin() {
		return standSkin;
	}
	
	
	public boolean onlyVisibleToStandUsers = true;
	public boolean standCanHaveNoPhysics = true;
	public boolean standHasNoGravity = true;
	public boolean canOnlyHurtFromStands = true;
	public boolean healthLinkedWithUser = true;
	public void setIsPhysicalObject() {
		onlyVisibleToStandUsers = false;
		standCanHaveNoPhysics = false;
		standHasNoGravity = false;
		canOnlyHurtFromStands = false;
		healthLinkedWithUser = false;
	}
	
	@Override
	public boolean isInvisible() {
		return clientCantSeeThisStand() || super.isInvisible();
	}
	
	@Override
	public boolean isInvisibleTo(Player player) {
		return clientCantSeeThisStand() || super.isInvisibleTo(player);
	}

	@Override
	public boolean displayFireAnimation() {
		return !clientCantSeeThisStand() && super.displayFireAnimation();
	}
	
	@Override
	public boolean onlyVisibleToStandUsers() {
		return onlyVisibleToStandUsers;
	}
	
	public final boolean isVisibleForAll() {
		return !onlyVisibleToStandUsers();
	}
	
	@Override
	public boolean canBeSeenByAnyone() {
		return isVisibleForAll() && super.canBeSeenByAnyone();
	}
	
	
	@Override
	public void push(Entity entity) {}
	
	@Override
	public boolean isPushable() { return false; }
	
	@Override
	public void pushEntities() {}

	@Override
	public boolean isPickable() {
		if (level().isClientSide()) {
			Player clientPlayer = ClientProxy.getClientPlayer();
			if (clientPlayer != null && this.is(ClientGlobals.playerStandEntity)) {
				return false;
			}
		}
		return super.isPickable();
	}
	
	
	public boolean requiresUser() {
		return healthLinkedWithUser;
	}
	
	@Override
	public boolean isInvulnerableTo(/*ServerLevel level, */DamageSource damageSource) {
		LivingEntity user = getUser();
		return user != null && (
					user.isInvulnerableTo(/*level, */damageSource)
					|| user instanceof Player player && player.getAbilities().invulnerable && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
				|| canOnlyHurtFromStands && !DamageUtil.canHurtStands(damageSource)
				|| super.isInvulnerableTo(/*level, */damageSource);
	}
	
	@Override
	protected float getDamageAfterMagicAbsorb(DamageSource dmgSource, float dmgAmount) {
		boolean isBlocking = isBlocking() && canBlockFromAngle(dmgSource.getSourcePosition());
		dmgAmount = super.getDamageAfterMagicAbsorb(dmgSource, dmgAmount);
		dmgAmount = standDamageResistance(dmgSource, dmgAmount, isBlocking);
		this.damageContainers.peek().setNewDamage(dmgAmount);
		return dmgAmount;
	}

	protected float standDamageResistance(DamageSource dmgSource, float dmgAmount, boolean isBlocking) {
		if (!dmgSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			float blockedRatio = 0;
			// TODO stand guard
			// TODO (stand guard) cancel user hurt sound when blocking an attack
//			if (isBlocking && userPower != null) {
//				blockedRatio = 1F;
//				if (userPower.usesStamina()) {
//					float staminaCost = StandStatFormulas.getBlockStaminaCost(dmgAmount);
//					float stamina = userPower.getStamina();
//					if (!userPower.consumeStamina(staminaCost)) {
//						blockedRatio = stamina / staminaCost;
//						standCrash();
//					}
//				}
//			}
//			Float multiplier = getCurrentTask().map(task -> task.getAction()
//					.getDamageBlockMultiplier(userPower, this, task)).orElse(null);
//			if (multiplier != null && multiplier != 0) {
//				blockedRatio += (1 - blockedRatio) * multiplier;
//			}
//			if (blockedRatio >= 1) {
//				wasDamageBlocked = true;
//				if (dmgSource.getEntity() instanceof StandEntity) {
//					((StandEntity) dmgSource.getEntity()).playPunchSound = false;
//				}
//			}
			float standResistanceDamage = dmgAmount * (1 - getPhysicalResistance(blockedRatio, dmgAmount));
			LivingEntity user = getUser();
			if (user != null) {
				float hypotheticalUserDamage = DamageUtil.damageEntityWillTake(user, dmgSource, 
						damageContainers.peek().getOriginalDamage(), true).getNewDamage();
				return Math.min(standResistanceDamage, hypotheticalUserDamage);
			}
			else {
				return standResistanceDamage;
			}
		}
		return dmgAmount;
	}

	protected float getPhysicalResistance(float blockedRatio, float damageDealt) {
		return StandStatFormulas.getPhysicalResistance(getDurability(), getAttackDamage(), blockedRatio, damageDealt);
	}
	
	@Override
	public void setHealth(float health) {
		if (healthLinkedWithUser) {
			redirectDamageToUser(health);
		}
		super.setHealth(health);
	}
	
	protected void redirectDamageToUser(float newHealthValue) {
		if (level() instanceof ServerLevel level) {
			LivingEntity user = getUser();
			if (user != null) {
				DamageContainer currentlyTakingDamage = !damageContainers.empty() ? damageContainers.peek() : null;
				if (currentlyTakingDamage != null && this.getHealth() - currentlyTakingDamage.getNewDamage() == newHealthValue) { // this means it is *very* likely being called in LivingEntity#actuallyHurt
					user.hurt(new StandLinkDamageSource(level, this, currentlyTakingDamage.getSource()), currentlyTakingDamage.getNewDamage());
				}
				else {
					user.setHealth(newHealthValue);
				}
			}
		}
	}

	@Override
	public void knockback(double strength, double xRatio, double zRatio) {
		LivingKnockBackEvent event = CommonHooks.onLivingKnockBack(this, (float) strength, xRatio, zRatio);
		if (event.isCanceled()) return;
		
		DamageContainer curDamage = !damageContainers.isEmpty() ? damageContainers.peek() : null;
		strength = event.getStrength();
		xRatio = event.getRatioX();
		zRatio = event.getRatioZ();
		strength *= 1.0F - (float) getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		if (isBlocking() && canBlockFromAngle(position().add(new Vec3(xRatio, 0, zRatio)))) {
			double durabilityStat = getDurability();
			strength *= StandStatFormulas.getBlockingKnockbackMult(durabilityStat);
		}

		if (strength > 0) {
			hasImpulse = true;
			Vec3 motionVec = getDeltaMovement();
			Vec3 knockbackVec = new Vec3(xRatio, 0, zRatio).normalize().scale(strength);
			setDeltaMovement(
					motionVec.x / 2 - knockbackVec.x, 
					this.onGround() ? Math.min(0.4, motionVec.y / 2 + strength) : motionVec.y, 
					motionVec.z / 2 - knockbackVec.z);
			RipplesModifiedDamageSource.afterKnockbackApplied(this, curDamage != null ? curDamage.getSource() : null);
		}

		if (healthLinkedWithUser) {
			LivingEntity user = getUser();
			if (user != null && user.isAlive()) {
				user.knockback(strength, xRatio, zRatio);
				RipplesModifiedDamageSource.afterKnockbackApplied(user, curDamage != null ? curDamage.getSource() : null);
				user.hurtMarked = true;
			}
		}
	}

	protected void tickHealth(LivingEntity user) {
		if (healthLinkedWithUser) {
			getAttribute(Attributes.MAX_HEALTH).setBaseValue(user.getMaxHealth());
			super.setHealth(user.isAlive() ? user.getHealth() : 0);
			deathTime = user.deathTime;
		}
	}
	
	public boolean isStandBlocking() {
		return false;
	}

	public boolean canBlockFromAngle(Vec3 dmgPosition) {
		// XXX disable blocking in time stop
//		if (!this.canUpdate()) {
//			return false;
//		}
		if (dmgPosition == null) {
			return false;
		}
		Vec3 viewVec = getViewVector(1.0F);
		Vec3 diffVec = dmgPosition.subtract(position()).normalize();
		return diffVec.dot(viewVec) > 0.5;
	}


	@Deprecated
	@Override
	public boolean canAttack(LivingEntity entity) {
		if (entity.is(this)) return false;

		LivingEntity user = getUser();
		if (user != null) {
			boolean canHarm = DamageUtil.isNotFriendlyFire(user, StandUtil.getStandUser(entity));
			if (canHarm && entity instanceof Animal) {
				canHarm &= !entity.isPassengerOfSameVehicle(user);
				if (canHarm && entity instanceof TamableAnimal tameable) {
					LivingEntity tameableOwner = tameable.getOwner();
					canHarm &= !(tameableOwner != null && tameableOwner == user);
				}
			}
			return canHarm;
		}

		return true;
	}

	public boolean canAttackEntity(Entity target) {
		if (target instanceof LivingEntity living) {
			return canAttack(living);
		}
		LivingEntity user = getUser();
		if (target instanceof Projectile projectile) {
			Entity owner = projectile.getOwner();
			if (owner != null && (owner.is(this) || owner.is(user))) {
				return target instanceof DamagingEntity modProjectile && modProjectile.canHitOwner();
			}
		}
		if (user != null && target.getControllingPassenger() == user) {
			return false;
		}
		return true;
	}
	
	
	protected NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
	public HandItemsAsInventory<StandEntity> handsPseudoInventory = new HandItemsAsInventory<>(this, handItems) {
		@Override
		public boolean stillValid(Player player) {
			return super.stillValid(player) && player.is(entity.getUser());
		}
	};
	
	protected void openStandHandsContainer() {
		if (!level().isClientSide()) {
			LivingEntity user = getUser();
			if (user instanceof ServerPlayer pl) {
				PlayerExternalContainers.get(pl).openMenu(StandHandsContainerMenu.createServerSide(this), null);
			}
		}
	}
	
	@Override
	public Iterable<ItemStack> getHandSlots() {
		return this.handItems;
	}
	
	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return Collections.emptyList();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return switch (slot.getType()) {
			case HAND -> this.handItems.get(slot.getIndex());
			default -> ItemStack.EMPTY;
		};
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		this.verifyEquippedItem(stack);
		switch (slot.getType()) {
			case HAND -> {
				this.onEquipItem(slot, this.handItems.set(slot.getIndex(), stack), stack);
			}
			default -> {}
		}
	}

	@Override
	public HumanoidArm getMainArm() {
		LivingEntity user = getUser();
		if (user != null) {
			return user.getMainArm();
		}
		return HumanoidArm.RIGHT;
	}
	
	@Nullable
	public HandOccupied getHandOccupiedBy(InteractionHand hand) {
		if (hand == InteractionHand.OFF_HAND && LivingComponentGrab.getEntityGrabbedBy(this) != null) {
			return HandOccupied.GRABBED_TARGET;
		}
		ItemStack heldItem = getItemInHand(hand);
		if (!heldItem.isEmpty()) {
			return HandOccupied.ITEM;
		}
		return null;
	}

	/**
	 * @return false it's not possible to place the entire stack in the inventory.
	 */
	public boolean addItem(ItemStack item) {
		return handsPseudoInventory.add(item);
	}
	
	@Override
	protected void dropEquipment(/*ServerLevel level*/) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			dropItem(slot);
		}
	}
	
	public void dropItem(EquipmentSlot slot) {
		Level level = level();
		if (!level.isClientSide()) {
			ItemStack item = getItemBySlot(slot);
			if (!item.isEmpty()) {
				Vec3 itemPos = position();
				InteractionHand hand = slot == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND
						: slot == EquipmentSlot.OFFHAND ? InteractionHand.OFF_HAND
								: null;
				Vec3 offset = new Vec3(
						hand != null ? getBbWidth() * 0.5 * (UtilFunctions.getHandSide(this, hand) == HumanoidArm.LEFT ? -1 : 1) : 0, 
						getBbHeight() * 0.4, 
						0);
				itemPos = itemPos.add(offset.yRot((180 - getYRot()) * MathUtil.DEG_TO_RAD));
				ItemEntity itemEntity = new ItemEntity(level, itemPos.x, itemPos.y, itemPos.z, item.copy());
				setItemSlot(slot, ItemStack.EMPTY);
				level.addFreshEntity(itemEntity);
			}
		}
	}

	@Nullable
	public ItemEntity tossItem(InteractionHand hand, boolean singleItem) {
		EquipmentSlot slot = switch (hand) {
			case MAIN_HAND -> EquipmentSlot.MAINHAND;
			case OFF_HAND -> EquipmentSlot.OFFHAND;
		};
		return tossItem(slot, getXRot(), getYRot(), singleItem ? 1 : Integer.MAX_VALUE);
	}

	@Nullable
	public ItemEntity tossItem(EquipmentSlot slot, Vec3 tossVec) {
		Vec2 angles = MathUtil.lookAngles(tossVec);
		float xRot = angles.x;
		float yRot = angles.y;
		return tossItem(slot, xRot, yRot, Integer.MAX_VALUE);
	}

	@Nullable
	public ItemEntity tossItem(EquipmentSlot slot, float xRot, float yRot, int maxCount) {
		Level level = level();
		if (level.isClientSide()) return null;

		ItemStack item = getItemBySlot(slot);
		if (item.isEmpty()) return null;
		
		if (maxCount < item.getCount()) {
			item = item.split(maxCount);
		}
		else {
			setItemSlot(slot, ItemStack.EMPTY);
		}

		ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getEyeY() - 0.3F, this.getZ(), item);
//		itemEntity.setPickUpDelay(40);
		itemEntity.setThrower(this);

		float f8 = Mth.sin(xRot * MathUtil.DEG_TO_RAD);
		float f2 = Mth.cos(xRot * MathUtil.DEG_TO_RAD);
		float f3 = Mth.sin(yRot * MathUtil.DEG_TO_RAD);
		float f4 = Mth.cos(yRot * MathUtil.DEG_TO_RAD);
		float f5 = this.random.nextFloat() * (float) (Math.PI * 2);
		float f6 = 0.02F * this.random.nextFloat();
		itemEntity.setDeltaMovement(
				(double)(-f3 * f2 * 0.3F) + Math.cos((double)f5) * (double)f6,
				(double)(-f8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
				(double)(f4 * f2 * 0.3F) + Math.sin((double)f5) * (double)f6
				);

		swing(slot == EquipmentSlot.OFFHAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
		level.addFreshEntity(itemEntity);
		return itemEntity;
	}
	
	public enum HandOccupied {
		ITEM,
		BLOCK,
		GRABBED_TARGET
	}

	@Override
	public ItemStack getProjectile(ItemStack shootable) {
		if (!(shootable.getItem() instanceof ProjectileWeaponItem)) {
			return ItemStack.EMPTY;
		} else {
			ProjectileWeaponItem weaponItem = (ProjectileWeaponItem) shootable.getItem();
			Predicate<ItemStack> projectileCondition = weaponItem.getSupportedHeldProjectiles(shootable);
			ItemStack projectile = ProjectileWeaponItem.getHeldProjectile(this, projectileCondition);
			if (!projectile.isEmpty()) {
				return CommonHooks.getProjectile(this, shootable, projectile);
			} else {
				projectileCondition = weaponItem.getAllSupportedProjectiles(shootable);

				for (InteractionHand hand : InteractionHand.values()) {
					ItemStack item = this.getItemInHand(hand);
					if (projectileCondition.test(item)) {
						return CommonHooks.getProjectile(this, shootable, item);
					}
				}

				return CommonHooks.getProjectile(this, shootable, ItemStack.EMPTY);
			}
		}
	}
	
	protected void pickUpItemEntities() {
		Level level = this.level();
		if (!level.isClientSide() && this.isManuallyControlled() && getCurStandAction() == null && this.getHealth() > 0) {
			AABB aabb;
			if (this.isPassenger() && !this.getVehicle().isRemoved()) {
				aabb = this.getBoundingBox().minmax(this.getVehicle().getBoundingBox()).inflate(1.0, 0.0, 1.0);
			} else {
				aabb = this.getBoundingBox().inflate(1.0, 0.5, 1.0);
			}

			List<Entity> list = this.level().getEntities(this, aabb);

			for (Entity entity : list) {
				if (!entity.isRemoved()) {
					this.touch(entity);
				}
			}
		}
	}
	
	protected void touch(Entity entity) {
		switch (entity) {
			case ItemEntity itemEntity -> {
				ItemStack itemStack = itemEntity.getItem();
				Item item = itemStack.getItem();
				int count = itemStack.getCount();
	
				// Neo: Fire item pickup pre/post and adjust handling logic to adhere to the event result.
//				TriState result = EventHooks.fireItemPickupPre(itemEntity, this).canPickup();
				TriState result = TriState.DEFAULT;
				if (result.isFalse()) {
					return;
				}
	
				// Make a copy of the original stack for use in ItemEntityPickupEvent.Post
				ItemStack originalCopy = itemStack.copy();
				// Subvert the vanilla conditions (pickup delay and target check) if the result is true.
				if ((itemEntity.getOwner() != this || !itemEntity.hasPickUpDelay() && itemEntity.tickCount > 40)
						&& (itemEntity.getTarget() == null || itemEntity.getTarget().equals(this.getUUID()))) {
					result = TriState.TRUE;
				}
				if (result.isTrue()) {
					boolean tookEntireStack = this.addItem(itemStack);
					if (tookEntireStack) {
						// Fire ItemEntityPickupEvent.Post
//						EventHooks.fireItemPickupPost(itemEntity, this, originalCopy);
						// Update `i` to reflect the actual pickup amount. Vanilla is wrong here and always reports the whole stack.
						count = originalCopy.getCount() - itemStack.getCount();

						this.take(itemEntity, count);
						if (itemStack.isEmpty()) {
							itemEntity.discard();
							itemStack.setCount(count);
						}

						if (getUser() instanceof ServerPlayer player) {
							player.awardStat(Stats.ITEM_PICKED_UP.get(item), count);
						}
						this.onItemPickup(itemEntity);
					}
				}
			}
			case AbstractArrow arrow -> {
				if (!(arrow instanceof ThrownTrident trident && !(trident.ownedBy(this) || trident.getOwner() == null))
						&& (/*arrow.isInGround()*/ arrow.inGround || arrow.isNoPhysics()) && arrow.shakeTime <= 0) {
					if (arrow.pickup == AbstractArrow.Pickup.ALLOWED && this.addItem(arrow.getPickupItem())) {
						this.take(arrow, 1);
						arrow.discard();
					}
				}
			}
			default -> {}
		}

	}
	

	protected float lastTickFinisherVal;
	protected float finisherVal;
	protected int noFinisherDecayTicks;
	protected static final int FINISHER_NO_DECAY_TICKS = 40;
	protected static final float FINISHER_DECAY = 0.025F;
    
	public float getFinisherMeter() {
		return entityData.get(FINISHER_VALUE);
	}
    
	public float getFinisherMeter(float partialTick) {
		return Mth.clamp(partialTick, lastTickFinisherVal, finisherVal);
	}
	
	public void setFinisherMeter(float value) {
		entityData.set(FINISHER_VALUE, Mth.clamp(value, 0, getFinisherMeterMax()));
	}
	
	public void addFinisherMeter(float value) {
		if (value > 0) {
			LivingEntity user = getUser();
			if (user != null && ModStatusEffects.isInResolveEffect(user)) {
				value *= 2;
			}
		}
		float prev = getFinisherMeter();
		setFinisherMeter(prev + value);
		this.noFinisherDecayTicks = Math.max(this.noFinisherDecayTicks, FINISHER_NO_DECAY_TICKS);
	}
	
	public void consumeFinisherMeter(float value) {
		float prev = getFinisherMeter();
		setFinisherMeter(prev - value);
		this.noFinisherDecayTicks = Math.max(this.noFinisherDecayTicks, FINISHER_NO_DECAY_TICKS);
	}
	
	public float getFinisherMeterMax() {
		return 2;
	}
	
	protected void tickFinisherMeter() {
		if (!level().isClientSide()) {
			if (noFinisherDecayTicks > 0) {
				noFinisherDecayTicks--;
			}
			else {
				EntityActionInstance currentAction = getCurStandAction();
				if (currentAction == null || !(currentAction.ability instanceof StandEntityAbility standAbility && standAbility.noFinisherBarDecay)) {
					float decay = FINISHER_DECAY;
					float value = entityData.get(FINISHER_VALUE);
					if (value < 1F) {
						decay *= 0.5F;
					}
					LivingEntity user = getUser();
					if (user != null && ModStatusEffects.isInResolveEffect(user)) {
						decay *= 0.5F;
					}
					setFinisherMeter(Math.max(value - decay, 0));
				}
			}
		}
		lastTickFinisherVal = finisherVal;
		finisherVal = entityData.get(FINISHER_VALUE);
	}

	
	/**
	 * Apparently we have to do this to make sure the user's id is read before the EntityJoinLevelEvent fires on client side.
	 */
	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity trackedEntity) {
		return new ClientboundAddEntityPacket(this, trackedEntity, entityData.get(USER_ID));
	}
	
	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket packet) {
		super.recreateFromPacket(packet);
		entityData.set(USER_ID, packet.getData());
	}
	
	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		ResourceLocation.STREAM_CODEC.encode(buffer, standId);
		buffer.writeFloat(yBodyRot);
		buffer.writeVarInt(tickCount);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		standId = ResourceLocation.STREAM_CODEC.decode(additionalData);
		yBodyRot = additionalData.readFloat();
		yBodyRotO = yBodyRot;
		tickCount = additionalData.readVarInt();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);

		ListTag listtag = new ListTag();
		for (ItemStack item : this.handItems) {
			if (!item.isEmpty()) {
				listtag.add(item.save(this.registryAccess()));
			} else {
				listtag.add(new CompoundTag());
			}
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);

		if (compound.contains("HandItems", 9)) {
			ListTag listtag = compound.getList("HandItems", 10);

			for (int i = 0; i < this.handItems.size(); i++) {
				CompoundTag itemNbt = listtag.getCompound(i);
				this.handItems.set(i, ItemStack.parseOptional(this.registryAccess(), itemNbt));
			}
		} else {
			this.handItems.replaceAll(item -> ItemStack.EMPTY);
		}
	}

	
	public int nonIdlePoseTimeStamp;
}
