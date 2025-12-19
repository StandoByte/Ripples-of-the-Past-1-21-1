package com.github.standobyte.jojo.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
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
import com.github.standobyte.jojo.util.target.ActionTarget;
import com.github.standobyte.jojo.util.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.target.AimingEntity;
import com.github.standobyte.jojo.util.target.HitResultUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityGrabAbility extends StandEntityAbility {

	public StandEntityGrabAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StandEntityGrab::new);
		usageGroup = AbilityUsageGroup.COMBAT;
		setDefaultPhaseLength(ActionPhase.WINDUP, 9);
		noFinisherBarDecay = true;
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		return super.isAbilityAvailable(context) && StandUtil.getStandGrabTarget(context) == null;
	}
	

	public static class StandEntityGrab extends EntityActionInstance {

		public StandEntityGrab(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			setStandOffset(0, 2, StandOffsetFromUser.Rotations.HEAD_XY, false);
			keepStandAimedAtTarget();
			aimAs = AimingEntity.STAND;
			tossStandHeldItems(EquipmentSlot.OFFHAND);
		}

		@Override
		public void actionPerformStart() {
			Level level = level();
			if (performer instanceof StandEntity standEntity) {
				ActionTarget target = HitResultUtil.clipEntityLook(standEntity, 
						entity -> !entity.is(standEntity.getUser()) && StandEntityPunchAbility.canStandHit(standEntity, entity) && !(
								entity instanceof LivingEntity living && (
										LivingComponentGrab.getEntityGrabbedBy(living) != null
										|| LivingComponentGrab.getEntityGrabbing(living) != null)), 
						0);
				if (!level.isClientSide()) {
					if (target.getType() == TargetType.ENTITY) {
						Entity targetEntity = target.getMainEntity();
						if (targetEntity instanceof LivingEntity targetLiving) {
							LivingComponentGrab standGrab = performer.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
							standGrab.setGrabTarget(targetLiving);
						}
					}
					StandPower standPower = StandPower.get(getPowerUser());
					standPower.consumeStamina(10);
				}
				
				if (target.getType() == TargetType.ENTITY) {
					standRotationTarget = target;
				}
				else {
					aimAs = AimingEntity.CAMERA_ENTITY;
				}
			}
		}

	}

}
