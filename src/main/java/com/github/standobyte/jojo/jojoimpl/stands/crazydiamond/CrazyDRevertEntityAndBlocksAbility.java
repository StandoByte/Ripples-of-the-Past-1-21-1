package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class CrazyDRevertEntityAndBlocksAbility extends StandEntityAbility {

	public CrazyDRevertEntityAndBlocksAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, RevertStateAction::new);
	}

	public static class RevertStateAction extends EntityActionInstance {

		public RevertStateAction(EntityActionType ability) {
			super(ability);
		}

	}

}
