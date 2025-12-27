package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility.StandEntityHeavyPunch;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

public class CrazyDMisshapingPunchInput extends Ability {

	public CrazyDMisshapingPunchInput(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		isSubAbility = true;
	}
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandEntity stand = PowerClass.STAND.cast(context).getSummonedStandEntity();
		if (stand != null) {
			EntityActionInstance curAction = stand.getCurStandAction();
			if (curAction != null && curAction instanceof StandEntityHeavyPunch punch && punch.finisherValue >= 1) {
				abilities.replaceOtherAbilityWith(context, "heavy_punch", this);
			}
		}
		
		return null;
	}

}
