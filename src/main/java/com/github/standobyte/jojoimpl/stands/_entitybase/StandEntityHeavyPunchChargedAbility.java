package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.EntityLingeringSoundInstance;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.damage.RipplesModifiedDamageSource;
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityHeavyPunchChargedAbility extends StandEntityAbility {

	public StandEntityHeavyPunchChargedAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityChargedHeavy::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, 16);
		setButtonHoldPhase(ActionPhase.WINDUP);
		setDefaultPhaseLength(ActionPhase.PERFORM, 6);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 12);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		return super.isAbilityAvailable(context) && StandUtil.getStandGrabTarget(context) == null;
	}
	
	
	public static class StandEntityChargedHeavy extends EntityActionInstance {
		protected float buttonChargeRatio;

		public StandEntityChargedHeavy(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			tossStandHeldItems(EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND);
		}
		
		@Override
		public void onButtonStopHold() {
			switch (getPhase()) {
				case BUTTON_CHARGE -> {
					phasesLength.put(ActionPhase.WINDUP, 0f);
					syncPhaseChanges();
				}
				case WINDUP -> {
					setPhaseStart(ActionPhase.PERFORM);
					syncPhaseChanges();
				}
				default -> {}
			}
		}
		
		@Override
		public void onSetPhase(ActionPhase newPhase) {
			if (getPhase() == ActionPhase.BUTTON_CHARGE && newPhase != ActionPhase.BUTTON_CHARGE) {
				this.buttonChargeRatio = getPhaseRatio();
			}
		}
		
		@Override
		public void actionPerformStart() {
			if (performer instanceof StandEntity stand) {
				setStandOffset(0, Math.max(stand.offsetFromUser.getRelativeOffset().z, 0) + 2,
						StandOffsetFromUser.Rotations.HEAD_XY,
						false);
				
				Level level = performer.level();
				if (level.isClientSide() && ClientGlobals.canHearStands) {
					ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_SWING.get(), stand), 
							stand.getSoundSource(), 1, 1, stand, stand.level()));

					ClientsideSoundsHelper.playNonVanillaClassSound(new EntityLingeringSoundInstance(ClientsideSoundsHelper.withStandSkin(
							ModSoundEvents.STAND_PUNCH_HEAVY_CRY.get(), stand), 
							stand.getSoundSource(), 1, 1, stand, stand.level()));
				}
			}
			aimAs = AimingEntity.STAND;
		}
		
		@Override
		public void actionPerformEnd() {
			Level level = level();
			if (performer instanceof StandEntity stand) {
				ActionTarget target = HitResultUtil.clipEntityLook(stand, entity -> StandEntityPunchAbility.canStandHit(stand, entity), 0);
				if (!level.isClientSide()) {
					StandPower standPower = StandPower.get(getPowerUser());
					
					if (StandEntityPunchAbility.playHitSound(target, level)) {
						StandUtil.broadcastSound((ServerLevel) level, target.getCenterPos(), 
								ModSoundEvents.STAND_PUNCH_HEAVY_CHARGED, true, standPower, 
								stand.getSoundSource(), 1, 1);
					}
					
					if (target.getType() == TargetType.ENTITY) {
						Entity targetEntity = target.getMainEntity();
						if (targetEntity instanceof LivingEntity targetLiving) {
                            DamageSource dmgSource = makePunchDamageSource();
							((RipplesModifiedDamageSource) dmgSource).jojo_ripples$modifyKnockback(2.5f, 1);
							float dmgAmount = 27.75f;
							standEntityAttack(stand, targetLiving, dmgSource, dmgAmount);
						}
					}
					
					punchedTarget = target;
					standPower.consumeStamina(100);
				}
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return phase.ordinal() < ActionPhase.PERFORM.ordinal();
		}
		
	}

}
