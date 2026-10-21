package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;

public class ConfigDoubleEffRange extends ConfigDouble {
	private ConfigDouble maxRangeConfig;

	public ConfigDoubleEffRange(double defaultValue) {
		super(defaultValue);
	}
	
	public void linkMaxRangeField(ConfigDouble rangeMax) {
		this.maxRangeConfig = rangeMax;
	}

	@Override
	public void set(Double value, CommonConfigState cfgState) {
		super.set(value, cfgState);
		if (value > maxRangeConfig.getAsDouble()) {
			maxRangeConfig.set(value);
		}
	}

}
