package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.util.target.ActionTarget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityGrabHeavyPunch extends StandEntityHeavyPunchAbility {

	public StandEntityGrabHeavyPunch(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.GRAB;
		isSubAbility = true;
		this.spriteName = abilityId.nameInMoveset().replace("grab_", "");
		this.name = Component.translatable("jojo_ripples.ability." + spriteName);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new GrabHeavyPunch(this);
	}
	
	public static class GrabHeavyPunch extends StandEntityHeavyPunchAbility.StandEntityHeavyPunch {
		protected LivingEntity punchTarget;

		public GrabHeavyPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onActionSet(EntityActionInstance prevAction) {
			super.onActionSet(prevAction);
			if (performer instanceof StandEntity standEntity && standEntity.offsetFromUser.grabIdleOffset != null) {
				standEntity.offsetFromUser.setOffset(
						standEntity.offsetFromUser.grabIdleOffset, 
						StandOffsetFromUser.Rotations.HEAD);
			}
		}
		
		@Override
		public void actionTick() {
			super.actionTick();

			if (punchTarget == null) {
				Level level = performer.level();
				int ticksDiff = (int) (calcFullTicks(ActionPhase.PERFORM, 0) - getFullTicksPassed());
				if (ticksDiff <= 4) {
					LivingComponentGrab standGrab = performer.getData(ModDataAttachmentTypes.LIVING_GRAB.get());
					if (standGrab != null) {
						LivingEntity grabbed = standGrab.getGrabbedEntity();
						if (grabbed != null) {
							punchTarget = grabbed;
							
							if (!level.isClientSide()) {
								standGrab.setGrabTarget(null);
								grabbed.setDeltaMovement(0, 0.75, 0);
								grabbed.hurtMarked = true;
							}
						}
					}
				}
			}
		}
		
		@Override
		protected ActionTarget getPunchTarget(StandEntity stand) {
			return new ActionTarget(punchTarget);
		}
		
	}

}
