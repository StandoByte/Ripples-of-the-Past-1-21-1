package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;

public class ConfigDoubleMaxRange extends ConfigDouble {
	private ConfigDouble effRangeConfig;

	public ConfigDoubleMaxRange(double defaultValue) {
		super(defaultValue);
	}
	
	public void linkEffRangeField(ConfigDouble rangeEffective) {
		this.effRangeConfig = rangeEffective;
	}

	@Override
	public void set(Double value, CommonConfigState cfgState) {
		super.set(value, cfgState);
		if (value < effRangeConfig.getAsDouble()) {
			effRangeConfig.set(value);
		}
	}

}
