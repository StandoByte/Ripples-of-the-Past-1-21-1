package com.github.standobyte.jojo.powersystem.ability.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;

public class AvailableAbilities {
	public final PowerClass<?> powerClass;
	public final Map<String, AbilityConditionCheck> _inMoveset = new HashMap<>();
	public final Map<String, Ability> inMovesetAndCanBeUsed = new HashMap<>();
	
	public AvailableAbilities(PowerClass<?> powerClass) {
		this.powerClass = powerClass;
	}
	
	public void update(Power<?> context, Moveset baseMoveset) {
		_inMoveset.clear();
		
		for (var baseAbilityEntry : baseMoveset.abilities.entrySet()) {
			Ability ability = baseAbilityEntry.getValue();
			ability = Ability.tryReplaceWithSubAbility(ability, context);
			if (ability.isAbilityAvailable(context)) {
				AbilityConditionCheck container = getContainerFor(ability);
				_inMoveset.put(baseAbilityEntry.getKey(), container);
			}
		}
		
		__visibleIter.clear();
		__visibleIter.addAll(_inMoveset.values());
		for (AbilityConditionCheck ability : __visibleIter) {
			ability.ability.onConditionCheck(context, this, ability);
		}
		
		inMovesetAndCanBeUsed.clear();
		for (var abilityEntry : _inMoveset.entrySet()) {
			AbilityConditionCheck ability = abilityEntry.getValue();
			if (ability.conditionCheck.isPositive()) {
				inMovesetAndCanBeUsed.put(abilityEntry.getKey(), ability.ability);
			}
		}
	}
	
	
	public void setConditionCheck(String baseAbilityName, ConditionCheck check) {
		AbilityConditionCheck container = _inMoveset.get(baseAbilityName);
		if (container != null) {
			container.conditionCheck = check;
		}
	}
	
	@Nonnull
	public ConditionCheck getConditionCheck(Ability ability) {
		return getConditionCheck(ability.abilityId.nameInMoveset());
	}

	@Nonnull
	public ConditionCheck getConditionCheck(String baseAbilityName) {
		AbilityConditionCheck container = _inMoveset.get(baseAbilityName);
		return container != null ? container.conditionCheck : ConditionCheck.NEGATIVE;
	}

	
	private final Map<AbilityId, AbilityConditionCheck> __cache = new HashMap<>();
	private final Collection<AbilityConditionCheck> __visibleIter = new ArrayList<>();
	
	private AbilityConditionCheck getContainerFor(Ability ability) {
		AbilityConditionCheck container = __cache.compute(ability.abilityId, (id, existing) -> {
			if (existing == null) return new AbilityConditionCheck(ability);
			else {
				existing.clear();
				return existing;
			}
		});
		container.ability = ability;
		return container;
	}
	
	@ApiStatus.Internal
	public static class AbilityConditionCheck {
		public Ability ability;
		public ConditionCheck conditionCheck;
		public int clientInputState;
		
		private AbilityConditionCheck(Ability ability) {
			this.ability = ability;
			this.conditionCheck = ConditionCheck.POSITIVE;
		}
		
		private void clear() {
			this.conditionCheck = ConditionCheck.POSITIVE;
		}
	}
	
}
