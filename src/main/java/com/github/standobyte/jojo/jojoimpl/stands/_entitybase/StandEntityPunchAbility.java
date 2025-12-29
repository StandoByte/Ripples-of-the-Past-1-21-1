package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods._EntitySelector;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StandEntityPunchAbility extends StandEntityAbility {

	public StandEntityPunchAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityPunch::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.WINDUP, 4);
		setDefaultPhaseLength(ActionPhase.PERFORM, 2);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
		noFinisherBarDecay = true;
	}
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			Moveset moveset = standPower.getMoveset();
			
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				if (LivingComponentGrab.getEntityGrabbedBy(standEntity) != null) {
					return moveset.getAbility("grab_punch");
				}
			}
			
			Ability punch = getComboPunch(standEntity, moveset);
			if (punch != null) return punch;
		}
		return super.replaceWithSubAbility(context, abilities);
	}
	
	
	@Override
	public void initActionFromConfig(EntityActionInstance action, Level level, LivingEntity standUser, LivingEntity standEntity) {
		super.initActionFromConfig(action, level, standUser, standEntity);
		if (!level.isClientSide()) {
			
		}
	}
	
	public static class StandEntityPunch extends EntityActionInstance {
		protected boolean playedSwingSound;
		protected boolean playedStandCrySound;

		public StandEntityPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			playedStandCrySound = prevAction != null;
			setStandOffset(0, 2, StandOffsetFromUser.Rotations.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
		}
		
		@Override
		public void actionTick() {
			Level level = performer.level();
			if (level.isClientSide() && ClientGlobals.canHearStands && !(playedSwingSound && playedStandCrySound) && performer instanceof StandEntity stand) {
				if (!playedSwingSound) {
					// how many ticks are left before the start of the 'perform' phase (when actionPerformStart() is called)
					int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
					if (ticksDiff <= 2) {
						level.playLocalSound(stand.getX(), stand.getEyeY(), stand.getZ(), ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_SWING.get(), stand), 
								stand.getSoundSource(), 1, 1, false);
						playedSwingSound = true;
					}
				}
				
				if (!playedStandCrySound) {
					if (!stand.isArmsOnlyMode()) {
						ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.STAND_PUNCH_CRY.get(), stand), 
								stand.getSoundSource(), 1, 1, stand, stand.level()));
					}
					playedStandCrySound = true;
				}
			}
		}
		
		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity stand) {
				ActionTarget target = getPunchTarget(stand);
				if (!level.isClientSide()) {
					StandPower standPower = StandPower.get(getPowerUser());
					
					if (playHitSound(target, level)) {
						StandUtil.broadcastSound((ServerLevel) level, target.getCenterPos(), 
								ModSoundEvents.STAND_PUNCH_LIGHT, true, standPower, 
								stand.getSoundSource(), 1, 1);
					}
					
					stand.addFinisherMeter(0.2f);
					if (target.getType() == TargetType.ENTITY) {
						Entity targetEntity = target.getMainEntity();
						if (targetEntity instanceof LivingEntity targetLiving) {
							var damageType = DamageUtil.type(level, ModDamageTypes.STAND_ATTACK);
							DamageSource dmgSource = new DamageSource(damageType, performer);
							float dmgAmount = StandStatFormulas.getLightAttackDamage(stand.getAttackDamage());
							if (standEntityAttack(stand, targetLiving, dmgSource, dmgAmount)) {
								stand.addFinisherMeter(0.2f);
							}
						}
					}

					punchedTarget = target;
					standPower.consumeStamina(10);
				}
				/*
				 *  During the punch, the Stand entity keeps rotating towards the target (keepStandAimedAtTarget()).
				 *  Additionally, when we set aimAs == AimingEntity.STAND, 
				 *  effectively this makes the Stand locked on the target entity after the punch,
				 *  because the Stand keeps aiming at *its* direction rather than the player's.
				 *  Here, if the Stand does not hit an entity, we reset this field to AimingEntity.PLAYER, 
				 *  resetting the aim back to the look direction of the user.
				 */
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.PLAYER;
				}
			}
		}
		
		protected ActionTarget getPunchTarget(StandEntity stand) {
			return StandEntityPunchAbility.aimAtPunchTarget(stand);
		}
		
	}
	
	
	public static ActionTarget aimAtPunchTarget(StandEntity stand) {
		return HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
	}
	
	public static boolean canStandHit(StandEntity stand, Entity target) {
		return canStandPick(stand, target) && stand.canAttackEntity(target);
	}
	
	// TODO make stand entities not pickable, add a separate OR predicate to CAN_BE_PICKED
	public static boolean canStandPick(StandEntity stand, Entity target) {
		return _EntitySelector.CAN_BE_PICKED.test(target);
	}
	
	public static boolean playHitSound(ActionTarget target, Level level) {
		if (target.isEmpty(level)) {
			return false;
		}
		return switch (target.getType()) {
			case ENTITY -> true;
			case BLOCK -> {
				BlockPos blockPos = target.getBlockPos();
				BlockState blockState = level.getBlockState(blockPos);
				yield blockState.getDestroySpeed(level, blockPos) != 0;
			}
			default -> false;
		};
	}
	
	
	// 
	
	@Deprecated
	protected List<String> punchNames = Util.make(new ArrayList<>(), list -> {
		list.add("punch");
		list.add("punch2");
		list.add("punch3");
		list.add("punch4");
	});
	
	@Deprecated
	protected Ability getComboPunch(StandEntity standEntity, Moveset moveset) {
		int startFromPunch = 0;
		
		if (standEntity != null) {
			if (LivingComponentGrab.getEntityGrabbedBy(standEntity) != null) {
				return moveset.getAbility("grab_punch");
			}
			
			
			AbilityId curAbility = LivingComponentAction.getComponent(standEntity).comboString.getLast();
			
			if (curAbility != null) {
				String actionName = curAbility.nameInMoveset();
				for (int i = 0; i < punchNames.size(); i++) {
					if (punchNames.get(i).equals(actionName)) {
						startFromPunch = i + 1;
						break;
					}
				}
			}
		}
		
		int size = punchNames.size();
		for (int i = 0; i < size; i++) {
			int index = (startFromPunch + i) % size;
			String nextPunchName = punchNames.get(index);
			Ability nextPunch = moveset.getAbility(nextPunchName);
			if (nextPunch != null) {
				return nextPunch;
			}
		}
		
		return null;
	}

}
