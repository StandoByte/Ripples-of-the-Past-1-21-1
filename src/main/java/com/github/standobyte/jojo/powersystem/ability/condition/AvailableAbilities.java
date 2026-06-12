package com.github.standobyte.jojo.powersystem.ability.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.finisher.StandFinisherCheck;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;

import net.minecraft.world.entity.LivingEntity;

public class AvailableAbilities {
	public final Map<String, AbilityConditionCheck> _inMoveset = new HashMap<>();
	public final Map<String, Ability> inMovesetAndCanBeUsed = new HashMap<>();
	public StandFinisherCheck standFinisherCheckLast = new StandFinisherCheck();
	
	public AvailableAbilities() {}
	
	public void update(Power<?> power, Moveset baseMoveset) {
		_inMoveset.clear();
		
		// Filtering out the abilities that are currently available (unlocked / make sense in the context)
		
		Map<String, Ability> abilities = baseMoveset.abilities;
		for (var baseAbilityEntry : abilities.entrySet()) {
			Ability ability = baseAbilityEntry.getValue();
			if (ability.isAbilityAvailable(power)) {
				AbilityConditionCheck container = getContainerFor(ability);
				container.clear();
				_inMoveset.put(baseAbilityEntry.getKey(), container);
			}
		}
		
		AbilityUsageContext ctxInstance = new AbilityUsageContext();
		LivingEntity user = power.getUser();
		ctxInstance.power = power;
		ctxInstance.isFrozenInTime = TimeStopEffect.getIsFrozenInTime(user);
		
		// Checking usage conditions on all of the abilities (this would make the ability gray out in the HUD if you currently can't use it for some reason)
		
		Collection<AbilityConditionCheck> visibleIter = this.__visibleIter; // to avoid ConcurrentModificationException
		visibleIter.clear();
		visibleIter.addAll(_inMoveset.values());
		for (AbilityConditionCheck ability : visibleIter) {
			ability.ability.onConditionCheck(ctxInstance, this, ability);
		}
		
		// Finisher stuff to replace base attacks with finishers
		
		standFinisherCheckLast.update(power, baseMoveset, this);
		
		// Ability replacing with dynamic polymorphism
		
		for (var abilityEntry : _inMoveset.entrySet()) {
			AbilityConditionCheck abilityContainer = abilityEntry.getValue();
			Ability contextVariation = abilityContainer.ability.replaceWithSubAbility(power, this);
			if (contextVariation != null && contextVariation.isAbilityAvailable(power)) {
				abilityEntry.setValue(getContainerFor(contextVariation));
			}
		}
		
		inMovesetAndCanBeUsed.clear();
		for (var abilityEntry : _inMoveset.entrySet()) {
			AbilityConditionCheck ability = abilityEntry.getValue();
			if (ability.conditionCheck.positive()) {
				inMovesetAndCanBeUsed.put(abilityEntry.getKey(), ability.ability);
			}
		}
	}
	private final Collection<AbilityConditionCheck> __visibleIter = new ArrayList<>();
	
	
	public void replaceOtherAbilityWith(Power<?> context, String baseAbilityName, Ability subAbility) {
		if (subAbility.isAbilityAvailable(context)) {
			AbilityConditionCheck container = getContainerFor(subAbility);
			_inMoveset.put(baseAbilityName, container);
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
		return getConditionCheck(ability.name());
	}

	@Nonnull
	public ConditionCheck getConditionCheck(String baseAbilityName) {
		AbilityConditionCheck container = _inMoveset.get(baseAbilityName);
		return container != null ? container.conditionCheck : ConditionCheck.NEGATIVE;
	}
	
	@Deprecated @Nullable public AbilityConditionCheck getAbilityResolved(Ability baseAbility) { return _inMoveset.get(baseAbility.name()); }
	@Deprecated @Nullable public AbilityConditionCheck getAbilityResolved(String baseAbilityName) { return _inMoveset.get(baseAbilityName); }
	@Nullable public AbilityConditionCheck getContextVariationContainer(Ability baseAbility) { return _inMoveset.get(baseAbility.name()); }
	@Nullable public AbilityConditionCheck getContextVariationContainer(String baseAbilityName) { return _inMoveset.get(baseAbilityName); }
	@Nullable public Ability getContextVariation(String baseAbilityName) { 
		AbilityConditionCheck container = _inMoveset.get(baseAbilityName);
		return container != null ? container.ability : null;
	}

	
	private final Map<AbilityId, AbilityConditionCheck> __cache = new HashMap<>();
	
	@ApiStatus.Internal
	public AbilityConditionCheck getContainerFor(Ability ability) {
		AbilityConditionCheck container = __cache.compute(ability.abilityId, (id, existing) -> {
			if (existing == null) return new AbilityConditionCheck(ability);
			else {
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
