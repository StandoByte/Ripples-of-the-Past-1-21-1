package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class CrazyDRevertEntityAndBlocksAbility extends StandEntityAbility {

	public CrazyDRevertEntityAndBlocksAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, RevertStateAction::new);
		setButtonHoldPhase(ActionPhase.PERFORM);
	}

	public static class RevertStateAction extends CrazyDHealAbility.HealingAction {

		public RevertStateAction(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}

	}

}
