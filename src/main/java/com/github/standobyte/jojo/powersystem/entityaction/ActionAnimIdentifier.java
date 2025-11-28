package com.github.standobyte.jojo.powersystem.entityaction;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.util.StringUtil;
import com.mojang.datafixers.util.Pair;

public record ActionAnimIdentifier(String name, int index, boolean isIdle) {

	protected ActionAnimIdentifier(String name, boolean isIdle) {
		this(name, 0, isIdle);
	}

	private static final Map<ActionAnimIdentifier, ActionAnimIdentifier> ANIM_IDS = new HashMap<>();
	/**
	 * Automatically splits the number at the end of the animation name.
	 */
	public static ActionAnimIdentifier getOrCreate(String animName, boolean setIdle) {
		Pair<String, OptionalInt> enumeratedName = StringUtil.splitIntAtTheEnd(animName);
		ActionAnimIdentifier anim = new ActionAnimIdentifier(
				enumeratedName.getFirst(), 
				enumeratedName.getSecond().orElse(1) - 1 /* 1-based indexing in anims */, 
				setIdle);
		ActionAnimIdentifier present = ANIM_IDS.get(anim);
		if (present != null) {
			return present;
		}
		else {
			ANIM_IDS.put(anim, anim);
			return anim;
		}
	}
	
	public static ActionAnimIdentifier getOrCreate(AbilityId abilityId) {
		return getOrCreate(abilityId.nameInMoveset(), false);
	}
	
	@Override
	public String toString() {
		String name = index > 0 ? this.name + index : this.name;
		if (isIdle) {
			name += " (idle)";
		}
		return name;
	}
	
	// no need to override equals() and hashCode() for records
}
