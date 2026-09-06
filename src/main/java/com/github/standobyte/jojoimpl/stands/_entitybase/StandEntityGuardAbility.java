package com.github.standobyte.jojoimpl.stands._entitybase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;
import com.github.standobyte.jojo.entityattachment.syncheddata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AbilityUsageContext;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.JojoModUtil;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class StandEntityGuardAbility extends StandEntityAbility {

	public StandEntityGuardAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, GuardAction::new);
		setDefaultPhaseLength(ActionPhase.PERFORM, 999999);
	}
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				UserStandEffects userEffects = standPower.userStandEffects;
				var toggleEffectType = ModStandAbilities.GUARD_TOGGLE.get();
				
				Optional<GuardToggle> guardEnabled = userEffects.getEffectOfType(toggleEffectType);
				if (guardEnabled.isEmpty()) {
					GuardToggle guardEnable = toggleEffectType.create(level);
					userEffects.addEffect(guardEnable);
					//return guardEnable; // for when it was a held ability
				}
				else {
					guardEnabled.get().remove();
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ConditionCheck checkConditions(AbilityUsageContext context) {
		ConditionCheck check = super.checkConditions(context);
		if (check.positive() && isGuardActive(PowerClass.STAND.cast(context.power))) {
			check = ConditionCheck.GREEN_HIGHLIGHT;
		}
		return check;
	}
	
	public static boolean isGuardActive(StandPower standPower) {
		return standPower != null && standPower.userStandEffects.getEffectOfType(ModStandAbilities.GUARD_TOGGLE.get()).isPresent();
	}
	
	
	public static class GuardToggle extends StandEffectInstance implements HeldInput {

		public GuardToggle(EntityCustomEffectType<?> effectType) {
			super(effectType);
		}

		@Override
		protected void start() {
			if (!entity.level().isClientSide()) {
				checkSetAction();
			}
		}

		@Override
		public void onKeyRelease(LivingEntity user) {
			if (!entity.level().isClientSide()) {
				remove();
			}
		}

		@Override
		protected void tick() {
			if (!entity.level().isClientSide()) {
				LivingEntity entity = getStandUser();
				StandEntity standEntity = StandUtil.getSummonedStand(entity);
				if (standEntity == null) {
					remove();
					return;
				}
				
				checkSetAction();
			}
		}
		
		@Nullable
		public HeldInput checkSetAction() {
			if (!entity.level().isClientSide()) {
				LivingEntity standUser = getStandUser();
				StandEntity standEntity = StandUtil.getSummonedStand(standUser);
				if (standEntity != null) {
					LivingComponentAction actionComponent = LivingComponentAction.getComponent(standEntity);
					EntityActionInstance curAction = actionComponent.getAction();
					if (curAction == null) {
						return setAction(actionComponent, standUser, standEntity);
					}
				}
			}
			return null;
		}
		
		public HeldInput setAction(LivingComponentAction actionComponent, LivingEntity standUser, LivingEntity standEntity) {
			StandEntityGuardAbility guardAbility = getGuardAbility();
			if (guardAbility != null) {
				EntityActionInstance guardAction = guardAbility.initActionOnAbilityUse(level, standUser, standEntity, null);
				HeldInput action_ = actionComponent.setAction(guardAction, SyncType.TRACKING_AND_SELF);
				return action_;
			}
			return null;
		}
		
		public boolean isGuard(EntityActionInstance curAction) {
			return curAction != null && curAction.ability == getGuardAbility();
		}
		
		Optional<StandEntityGuardAbility> guardAbilityQueried = null;
		public StandEntityGuardAbility getGuardAbility() {
			if (guardAbilityQueried == null) {
				if (userPower != null) {
					Ability ability = userPower.getAbility("guard");
					if (ability instanceof StandEntityGuardAbility guardAbility) {
						this.guardAbilityQueried = Optional.of(guardAbility);
					}
					else {
						this.guardAbilityQueried = Optional.empty();
					}
				}
			}
			
			return guardAbilityQueried != null ? guardAbilityQueried.orElse(null) : null;
		}

		@Override
		protected void stop() {
			if (!entity.level().isClientSide()) {
				LivingEntity entity = getStandUser();
				StandEntity standEntity = StandUtil.getSummonedStand(entity);
				if (standEntity != null) {
					LivingComponentAction actionComponent = LivingComponentAction.getComponent(standEntity);
					EntityActionInstance curAction = actionComponent.getAction();
					if (isGuard(curAction)) {
						curAction.forceStop();
						curAction.syncPhaseChanges();
					}
				}
			}
		}
	}
	
	// Is called right when an action ends, to seamlessly transition to guarding
	public static boolean onStandActionOver(LivingEntity actionPerformer, LivingComponentAction actionComponent) {
		if (actionPerformer instanceof StandEntity standEntity) {
			LivingEntity standUser = standEntity.getUser();
			StandPower standData = standEntity.getUserPower();
			if (standUser != null && standData != null) {
				GuardToggle guardToggle = standData.userStandEffects.getEffectOfType(ModStandAbilities.GUARD_TOGGLE.get()).orElse(null);
				if (guardToggle != null) {
					var guardAction = guardToggle.setAction(actionComponent, standUser, standEntity);
					return guardAction != null;
				}
			}
		}
		
		return false;
	}


	public static class GuardAction extends EntityActionInstance implements SyncedDataHolderExtended {
		public static final DataParameter<Float> BLOCKED_DAMAGE = DataParameter.defineId(GuardAction.class, EntityDataSerializers.FLOAT, 0f);
		protected final Map<UUID, MutableInt> blockedEntitiesTimers = new HashMap<>(); // fucking slimes (MC-169167 - bruh)

		public GuardAction(EntityActionType ability) {
			super(ability);
			userWalkSpeed = 0.3f;
		}

		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			setStandOffset(0, 0.5, StandOffsetFromUser.Rotations.HEAD_XY, false);
		}

		@Override
		public void actionTick() {
			if (!level().isClientSide()) {
				var iter = blockedEntitiesTimers.entrySet().iterator();
				while (iter.hasNext()) {
					var entityTimerEntry = iter.next();
					if (entityTimerEntry.getValue().decrementAndGet() <= 0) {
						iter.remove();
					}
				}
			}
		}

		@Override
		public void onButtonStopHold() {
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
		@Override
		public boolean onStandUnsummonCommand(LivingEntity user, StandPower standPower, StandType standType) {
			standPower.userStandEffects
					.getEffectsOfType(ModStandAbilities.GUARD_TOGGLE.get())
					.forEach(EntityCustomEffect::remove);
			forceStop();
			syncPhaseChanges();
			
			return false;
		}


		@Override
		public void defineSynchedData(Builder builder) {
			BLOCKED_DAMAGE.define(builder);
		}

		protected long lastGuardSoundTime = -1;
		protected static final long SOUND_DELAY = 100;
		@Override
		public <T> void onSyncedDataUpdated(T oldValue, T newValue, EntityDataAccessor<T> dataAccessor) {
			if (dataAccessor == BLOCKED_DAMAGE.param) {
				Level level = level();
				if (level.isClientSide()) {
					if (ClientGlobals.canSeeStands) {
						double dmgAmount = (Float) newValue;
						RandomSource random = performer.getRandom();
						Vec3 offset = new Vec3(
								(random.nextDouble() - 0.5) * 0.8,
								1.1 + random.nextDouble() * 0.4,
								performer.getBbWidth() / 2 + 0.1 + random.nextDouble() * 0.25)
								.yRot(-performer.yBodyRot * MathUtil.DEG_TO_RAD);
						Vec3 particlePos = performer.position().add(offset);
						level.addParticle(ModParticles.DAMAGE_GUARD.get(), 
								particlePos.x, particlePos.y, particlePos.z, dmgAmount, 0, 0);
					}
					if (ClientGlobals.canHearStands) {
						long time = Util.getMillis();
						if (lastGuardSoundTime < 0 || (time - lastGuardSoundTime) >= SOUND_DELAY) {
							StandEntity stand = (StandEntity) performer;
							ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
									ModSoundEvents.STAND_DAMAGE_GUARD.get(), stand), 
									stand.getSoundSource(), 
									1, 0.85f + performer.getRandom().nextFloat() * 0.3f, 
									stand, level));
							lastGuardSoundTime = time;
						}
					}
				}
			}
		}

	}

	
	public static boolean onStandBeingAttackedByNonStandAttack(StandEntity standEntity, DamageSource dmgSource, float dmgAmount) {
		if (!standEntity.level().isClientSide() && !standEntity.isDeadOrDying() && standEntity.isInvulnerableTo(dmgSource)
				&& standEntity.getCurStandAction() instanceof GuardAction guardAction) {
			DamageSourceModified _dmgSource = (DamageSourceModified) dmgSource;
			@Nullable Explosion explosion = _dmgSource.jojo_ripples$getCausingExplosion();
			@Nullable Entity directEntity = dmgSource.getDirectEntity();
			if (isBlockableDamage(dmgSource, explosion, directEntity)) {
				GuardAction.BLOCKED_DAMAGE.sendSignal(guardAction.synchedData, dmgAmount);
				return true;
			}
		}
		return false;
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onStartedStandTakesDamage(LivingDamageEvent.Pre event) {
		LivingEntity target = event.getEntity();
		if (target instanceof StandEntity standEntity) {
			EntityActionInstance standAction = standEntity.getCurStandAction();
			if (standAction instanceof GuardAction guardAction) {
				DamageContainer dmg = event.getContainer();
				DamageSource dmgSource = dmg.getSource();
				DamageSourceModified _dmgSource = (DamageSourceModified) dmgSource;
				
				@Nullable Explosion explosion = _dmgSource.jojo_ripples$getCausingExplosion();
				@Nullable Entity directEntity = dmgSource.getDirectEntity();
				if (isBlockableDamage(dmgSource, explosion, directEntity)) {
					Vec3 dmgPos = damagePos(dmgSource, explosion, directEntity);
					if (canBlockFromAngle(standEntity, dmgPos)) {
						float origDmgAmount = dmg.getOriginalDamage();

						BlockResult res = guardReduceDamage(AttackedEntity.STAND, standEntity, origDmgAmount);
						if (res.damageCoeff < 1) {
							float dmgAmount = dmg.getNewDamage();
							float newDmg = dmgAmount * res.damageCoeff;
							event.setNewDamage(newDmg);
							onReducedDamage(dmgSource, res.damageCoeff, res.outOfStamina, 
									newDmg, standEntity.getUser());
							GuardAction.BLOCKED_DAMAGE.sendSignal(guardAction.synchedData, origDmgAmount);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void beforePlayerTakesDamage(LivingIncomingDamageEvent event) {
		LivingEntity standUserTarget = event.getEntity();
		StandEntity standEntity = StandUtil.getSummonedStand(standUserTarget);
		if (standEntity != null) {
			// да повторение кода и чо ты мне сделаешь я в другмо городе
			EntityActionInstance standAction = standEntity.getCurStandAction();
			if (standAction instanceof GuardAction guardAction) {
				DamageContainer dmg = event.getContainer();
				DamageSource dmgSource = dmg.getSource();
				DamageSourceModified _dmgSource = (DamageSourceModified) dmgSource;
				
				@Nullable Explosion explosion = _dmgSource.jojo_ripples$getCausingExplosion();
				@Nullable Entity directEntity = dmgSource.getDirectEntity();
				if (isBlockableDamage(dmgSource, explosion, directEntity)) {
					Vec3 dmgPos = damagePos(dmgSource, explosion, directEntity);
					Vec3 userPos = getCenterPos(standUserTarget);
					boolean standIsBetweenDmgAndUser = standEntity.getBoundingBox().clip(dmgPos, userPos).isPresent();
					if (canBlockFromAngle(standEntity, dmgPos) && standIsBetweenDmgAndUser) {
						float origDmgAmount = dmg.getOriginalDamage();
						
						if (!standEntity.isInvulnerableTo(dmgSource)) {
							standEntity.hurt(dmgSource, origDmgAmount);
							event.setCanceled(true);
							return;
						}
						
						if (!dmgSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
							@Nullable UUID attackerId = directEntity != null ? directEntity.getUUID() : null;
							if ((float) standUserTarget.invulnerableTime > 10.0F || 
									attackerId != null && guardAction.blockedEntitiesTimers.containsKey(attackerId)) {
								event.setCanceled(true);
								return;
							}
							else {
								guardAction.blockedEntitiesTimers.put(attackerId, new MutableInt(10));
							}
						}

						BlockResult res = guardReduceDamage(AttackedEntity.USER, standEntity, origDmgAmount);
						if (res.damageCoeff < 1) {
							if (res.damageCoeff == 0) {
								event.setCanceled(true);
								
								// makes the entity bounce off (originally made this for slimes, then felt like trying to do this for any attacker)
					            if (directEntity != null) {
					            	Vec3 slimeMoveVec = directEntity.getDeltaMovement();
					            	// cos of XZ projections of attacker speed and stand look vector - makes sure it will bounce *away* from the stand
					            	if (
					            			new Vec3(slimeMoveVec.x, 0, slimeMoveVec.z).normalize().dot(
					            			standEntity.calculateViewVector(0, standEntity.getYRot())) < 0) {
					            		directEntity.setDeltaMovement(-slimeMoveVec.x, slimeMoveVec.y, -slimeMoveVec.z);
					            		JojoModUtil.forceSyncMotion(directEntity);
					            	}
					            }
							}
							else {
								float dmgAmount = dmg.getNewDamage();
								float newDmg = dmgAmount * res.damageCoeff;
								event.setAmount(newDmg);
								onReducedDamage(dmgSource, res.damageCoeff, res.outOfStamina, 
										newDmg, standUserTarget);
							}
							GuardAction.BLOCKED_DAMAGE.sendSignal(guardAction.synchedData, origDmgAmount);
						}
					}
				}
			}
		}
	}

	protected static class BlockResult {
		float damageCoeff = 1;
		boolean outOfStamina = false;
		
		static BlockResult instance = new BlockResult();
	}
	public static enum AttackedEntity { STAND, USER }
	
	protected static BlockResult guardReduceDamage(AttackedEntity attackedEntity, StandEntity standEntity, float origDmgAmount) {
		float blockedRatio = (float) standEntity.staminaCondition;
		boolean outOfStamina = false;

		StandPower userPower = standEntity.getUserPower();
		if (userPower != null && userPower.usesStamina()) {
			float staminaCost = StandStatFormulas.getGuardStaminaCost(origDmgAmount);
			float stamina = userPower.getStamina();

			if (!userPower.consumeStamina(staminaCost)) {
				blockedRatio *= stamina / staminaCost;
				outOfStamina = true;
				
				guardBreak(standEntity);
				
				var forceUnsummon = new StandEntityUnsummonAction.StandUnsummonInstance();
				forceUnsummon.isForced = true;
				forceUnsummon.moveTowardsPlayer = false;
				forceUnsummon.initDuration(5);
				standEntity.setAction(forceUnsummon);
			}
		}

		float dmgCoeff;
		switch (attackedEntity) {
			case STAND -> {
				dmgCoeff = 1 - 0.8f * blockedRatio;
				double furtherReductionCap = standEntity.getDurability() / 2;
				if (origDmgAmount < furtherReductionCap) {
					dmgCoeff *= origDmgAmount / furtherReductionCap;
				}
			}
			case USER -> {
				dmgCoeff = 1 - blockedRatio;
			}
			default -> throw new AssertionError();
		};
		
		BlockResult res = BlockResult.instance;
		res.damageCoeff = dmgCoeff;
		res.outOfStamina = outOfStamina;
		return res;
	}
	
	protected static void onReducedDamage(DamageSource dmgSource, float damageCoeff, boolean outOfStamina,
			float resultingDamage, LivingEntity targetStandUser) {
		DamageSourceModified _dmgSource = (DamageSourceModified) dmgSource;
		_dmgSource.jojo_ripples$modifyKnockback(_dmgSource.jojo_ripples$addKnockback(), 
				_dmgSource.jojo_ripples$knockbackMultiplier() * damageCoeff);
		
		if (!outOfStamina) {
			_dmgSource.jojo_ripples$setDisableKBImpact(true);
			_dmgSource.jojo_ripples$setNoHurtSound(true);
			_dmgSource.jojo_ripples$setNoFlinchModelAnim(true);
			float cameraTiltMultiplier = damageCoeff;
			if (targetStandUser != null) {
				cameraTiltMultiplier = Math.max(cameraTiltMultiplier, resultingDamage / targetStandUser.getMaxHealth() * 0.5f);
			}
			_dmgSource.jojo_ripples$setCameraTiltMultiplier(cameraTiltMultiplier);
		}
	}
	
	/* TODO guard break when out of stamina (stun the user for a bit) */
	public static void guardBreak(StandEntity standEntity) {
		StandPower standPower = standEntity.getUserPower();
		if (standPower != null) {
			standPower.userStandEffects
					.getEffectsOfType(ModStandAbilities.GUARD_TOGGLE.get())
					.forEach(EntityCustomEffect::remove);
		}
	}
	
	public static boolean isBlockableDamage(DamageSource dmgSource, @Nullable Explosion explosion, @Nullable Entity directEntity) {
		return explosion != null || directEntity != null && dmgSource.is(Tags.DamageTypes.IS_PHYSICAL);
	}
	
	public static Vec3 damagePos(DamageSource dmgSource, @Nullable Explosion explosion, @Nullable Entity directEntity) {
		return explosion != null ? explosion.center() : getCenterPos(directEntity);
	}

	public static boolean canBlockFromAngle(Entity blockingEntity, Vec3 dmgPosition) {
		if (dmgPosition == null) {
			return false;
		}
		
		Vec3 targetPos = getCenterPos(blockingEntity);
		Vec3 attackInvVec = dmgPosition.subtract(targetPos).normalize();
		float attackXRot = Mth.wrapDegrees((float)(-(Mth.atan2(
				attackInvVec.y, 
				Math.sqrt(
						attackInvVec.x * attackInvVec.x + 
						attackInvVec.z * attackInvVec.z))
				* MathUtil.RAD_TO_DEG)));
		
		float blockXRot = blockingEntity.getViewXRot(1);
		float blockYRot = blockingEntity.getViewYRot(1);
		blockXRot = Mth.clamp(attackXRot, 
				Math.max(blockXRot - 30, -90), 
				Math.min(blockXRot + 30, 90)); // an area of leniency for up-down rotation
		Vec3 blockVec = blockingEntity.calculateViewVector(blockXRot, blockYRot);
		
		return attackInvVec.dot(blockVec) > 0.5;
	}
	
	public static Vec3 getCenterPos(Entity entity) {
		return new Vec3(entity.getX(), entity.getY(0.5), entity.getZ());
	}

}
