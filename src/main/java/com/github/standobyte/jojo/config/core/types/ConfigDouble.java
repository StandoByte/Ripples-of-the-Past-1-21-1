package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigDouble extends ConfigOption<Double> {
	protected DefaultedValue.Double value;
	protected DefaultedValue.Double remoteValue;
	protected double minValue = -Double.MAX_VALUE;
	protected double maxValue = Double.MAX_VALUE;

	public ConfigDouble(double defaultValue) {
		super(PrimitiveCodec.DOUBLE, ByteBufCodecs.DOUBLE);
		this.value = new DefaultedValue.Double(defaultValue);
	}
	
	public ConfigDouble initRange(double minValue, double maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		return this;
	}

	public ConfigDouble initMinValue(double minValue) { return initRange(minValue, this.maxValue); }
	public ConfigDouble initMaxValue(double maxValue) { return initRange(this.minValue, maxValue); }
	public ConfigDouble initNonNegative() { return initRange(0, this.maxValue); }

	@Override
	public Double get(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value;
	}

	public double getAsDouble() {
		return getValueToUse(cfgState).value;
	}

	@Override
	public void set(Double value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = Mth.clamp(value, minValue, maxValue);
	}

	@Override
	public boolean isDefault(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value == getDefaultValue();
	}
	
	public double getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset(CommonConfigState cfgState) {
		getValueToUse(cfgState).reset();
	}
	
	@Override
	public void setRemoteConfig() {
		super.setRemoteConfig();
		remoteValue = new DefaultedValue.Double(getDefaultValue());
	}
	
	@Override
	public void unsetRemoteConfig() {
		super.unsetRemoteConfig();
		remoteValue = null;
	}
	
	protected DefaultedValue.Double getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
