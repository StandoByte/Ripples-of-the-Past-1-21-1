package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;

public class CrazyDMisshapingPunchInput extends Ability {

	public CrazyDMisshapingPunchInput(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		isSubAbility = true;
	}

}
