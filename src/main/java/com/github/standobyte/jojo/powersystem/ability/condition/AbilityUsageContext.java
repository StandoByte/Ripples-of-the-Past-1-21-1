package com.github.standobyte.jojo.powersystem.ability.condition;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.Power;

@ApiStatus.Internal
public class AbilityUsageContext {
	public Power<?> power;
	public boolean isFrozenInTime;
	
	public AbilityUsageContext() {}
	
	public AbilityUsageContext(Power<?> power, boolean isFrozenInTime) {
		this.power = power;
		this.isFrozenInTime = isFrozenInTime;
	}
}
