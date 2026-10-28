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
		adjustTheOtherRangeStat();
	}
	
	@Override
	public void reset(CommonConfigState cfgState) {
		super.reset(cfgState);
		adjustTheOtherRangeStat();
	}
	
	protected void adjustTheOtherRangeStat() {
		double value = getValueToUse(cfgState).value;
		if (value > maxRangeConfig.getAsDouble()) {
			maxRangeConfig.set(value);
		}
	}

}
