package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

public class CrazyDTerrainWallAbility extends CrazyDRestoreTerrainAbility {

	public CrazyDTerrainWallAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		this.createActionObj = WallCreation::new;
	}

	public static class WallCreation extends TerrainRestoration {

		public WallCreation(EntityActionType ability) {
			super(ability);
		}

	}

}
