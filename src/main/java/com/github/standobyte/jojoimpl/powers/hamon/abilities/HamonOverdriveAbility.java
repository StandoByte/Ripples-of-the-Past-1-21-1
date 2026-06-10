package com.github.standobyte.jojoimpl.powers.hamon.abilities;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

public class HamonOverdriveAbility extends EntityActionAbility {

	public HamonOverdriveAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, HamonOverdriveBeat::new);
		setDefaultPhaseLength(ActionPhase.WINDUP, 5);
		setDefaultPhaseLength(ActionPhase.PERFORM, 3);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 2);
	}

	public static class HamonOverdriveBeat extends EntityActionInstance {
		
		public HamonOverdriveBeat(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionPerformStart() {
		}
	}
	
}
