package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.target.ActionTarget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class StandEntityGrabBarrage extends StandEntityBarrageAbility {

	public StandEntityGrabBarrage(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.GRAB;
		isSubAbility = true;
		this.spriteName = abilityId.nameInMoveset().replace("grab_", "");
		this.name = Component.translatable("jojo_ripples.ability." + spriteName);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new GrabBarrage(this);
	}
	
	public static class GrabBarrage extends StandEntityBarrageAbility.StandEntityBarrage {

		public GrabBarrage(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionTick() {
			if (!level().isClientSide() && getPhase() == ActionPhase.PERFORM) {
				LivingEntity grabbedEntity = LivingComponentGrab.getEntityGrabbedBy(performer);
				if (grabbedEntity == null || !grabbedEntity.isAlive()) {
					setPhaseStart(ActionPhase.RECOVERY);
					syncPhaseChanges();
				}
			}
			super.actionTick();
		}
		
		@Override
		protected float getHitsPerTick(StandEntity stand) {
			return super.getHitsPerTick(stand) / 2; // the left arm is busy, duh
		}
		
		@Override
		protected ActionTarget getPunchTarget(StandEntity stand) {
			return new ActionTarget(LivingComponentGrab.getEntityGrabbedBy(stand));
		}
		
	}

}
