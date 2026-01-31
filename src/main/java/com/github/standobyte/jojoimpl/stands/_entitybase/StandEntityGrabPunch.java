package com.github.standobyte.jojoimpl.stands._entitybase;

import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.target.ActionTarget;

import net.minecraft.network.chat.Component;

public class StandEntityGrabPunch extends StandEntityPunchAbility {

	public StandEntityGrabPunch(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		usageGroup = AbilityUsageGroup.GRAB;
		isSubAbility = true;
		this.spriteName = this.name().replace("grab_", "");
		this.name = Component.translatable("jojo_ripples.ability." + spriteName);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return new GrabPunch(this);
	}
	
	public static class GrabPunch extends StandEntityPunchAbility.StandEntityPunch {

		public GrabPunch(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		protected ActionTarget getPunchTarget(StandEntity stand) {
			return new ActionTarget(LivingComponentGrab.getEntityGrabbedBy(stand));
		}
		
	}

}
