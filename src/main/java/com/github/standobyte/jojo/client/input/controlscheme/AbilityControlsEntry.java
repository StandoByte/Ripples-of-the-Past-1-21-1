package com.github.standobyte.jojo.client.input.controlscheme;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;

public record AbilityControlsEntry(PowerClass<?> powerClass, String abilityName) {
	
	public AbilityConditionCheck getAbility() {
		AvailableAbilities allAbilities = ClientPowerCache.getAvailableAbilities(this.powerClass);
		return allAbilities.getContextVariationContainer(this.abilityName);
	}
}
