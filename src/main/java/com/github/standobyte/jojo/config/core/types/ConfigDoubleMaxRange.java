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
		adjustTheOtherRangeStat();
	}
	
	@Override
	public void reset(CommonConfigState cfgState) {
		super.reset(cfgState);
		adjustTheOtherRangeStat();
	}
	
	protected void adjustTheOtherRangeStat() {
		double value = getValueToUse(cfgState).value;
		if (value < effRangeConfig.getAsDouble()) {
			effRangeConfig.set(value);
		}
	}

}
