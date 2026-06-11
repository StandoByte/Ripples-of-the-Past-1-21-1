package com.github.standobyte.jojo.client.input.controlscheme;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.controlscheme.AbilityControlScheme.InputsByKeyModifier;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;

import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.TriState;

public class AbilityHotbarSlot {
	public int index;
	public final InputsByKeyModifier binds = new InputsByKeyModifier();
	
	public AbilityHotbarSlot(int index) {
		this.index = index;
	}
	
	public InputsByKeyModifier getBinds() {
		return binds;
	}
	
	@Nullable
	public AbilityConditionCheck showAbility(KeyModifier curModifier) {
		for (InputMethod inputMethod : InputMethod.values()) {
			AbilityControlsEntry abilityEntry = this.binds.getFirst(curModifier, inputMethod);
			if (abilityEntry != null) {
				AbilityConditionCheck ability = abilityEntry.getAbility();
				if (ability != null) {
					boolean showAbility = AbilityInputState.showAbilityInHUD(ability, TriState.FALSE);
					if (showAbility) {
						return ability;
					}
				}
			}
		}
		
		if (curModifier != KeyModifier.NONE) {
			return showAbility(KeyModifier.NONE);
		}
		else {
			return null;
		}
	}
	
	public static int numberKey(int slotIndex) {
		if (slotIndex >= 0 && slotIndex < 9) {
			return slotIndex + 1;
		}
		if (slotIndex == 9) return 0;
		return -1;
	}
}
