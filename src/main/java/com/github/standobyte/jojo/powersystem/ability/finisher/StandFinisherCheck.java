package com.github.standobyte.jojo.powersystem.ability.finisher;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import it.unimi.dsi.fastutil.floats.Float2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMap;

public class StandFinisherCheck {
	protected final Map<String, Float2ObjectSortedMap<AbilityConditionCheck>> curFinishers = new HashMap<>();
	
	public void update(Power<?> context, Moveset baseMoveset, AvailableAbilities abilities) {
		Map<String, Float2ObjectSortedMap<AbilityConditionCheck>> finishers = this.curFinishers;
		finishers.values().forEach(Map::clear);

		float curFinisherValue = getFinisherValue(context);
		if (curFinisherValue < 0) {
			return;
		}
		
		Map<String, Ability> movesetAbilities = baseMoveset.abilities;
		for (var baseAbilityEntry : movesetAbilities.entrySet()) {
			Ability ability = baseAbilityEntry.getValue();
			AbilityStandFinisherData isFinisherOf = getAbilityFinisherOf(ability);
			if (isFinisherOf != null && isFinisherOf.minFinisherValue() <= curFinisherValue) {
				Float2ObjectSortedMap<AbilityConditionCheck> finishersOf = finishers.computeIfAbsent(isFinisherOf.baseAbilityName(), 
						__ -> new Float2ObjectRBTreeMap<>());
				finishersOf.put(isFinisherOf.minFinisherValue(), abilities.getContainerFor(ability));
			}
		}
		
		for (var finisherEntries : finishers.entrySet()) {
			String baseAbilityName = finisherEntries.getKey();
			Float2ObjectSortedMap<AbilityConditionCheck> finishersOf = finisherEntries.getValue();
			for (var finisherEntry : finishersOf.reversed().entrySet()) {
				AbilityConditionCheck ability = finisherEntry.getValue();
				if (ability.ability.isAbilityAvailable(context) && ability.conditionCheck.isPositive()) {
					abilities._inMoveset.put(baseAbilityName, ability);
					break;
				}
			}
		}
	}
	
	
	protected float getFinisherValue(Power<?> context) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				return standEntity.getFinisherMeter();
			}
		}
		
		return -1;
	}
	
	protected AbilityStandFinisherData getAbilityFinisherOf(Ability ability) {
		return ability.isStandFinisherOf;
	}
	
}
