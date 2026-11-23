package com.github.standobyte.jojo.powersystem.ability.condition;

import com.github.standobyte.jojo.powersystem.Power;

public class AbilityUsageContext {
	public Power<?> power;
	public boolean isFrozenInTime;
	
	public AbilityUsageContext(Power<?> power, boolean isFrozenInTime) {
		this.power = power;
		this.isFrozenInTime = isFrozenInTime;
	}
}
