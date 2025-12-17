package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;

public class CrazyDTerrainWallAbility extends StandEntityAbility {

	public CrazyDTerrainWallAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, WallCreation::new);
	}

	public static class WallCreation extends EntityActionInstance {

		public WallCreation(EntityActionType ability) {
			super(ability);
		}

	}

}
