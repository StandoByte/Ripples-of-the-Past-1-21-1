package com.github.standobyte.jojo.config.core.types;

import java.util.function.DoubleSupplier;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigDouble extends ConfigOption<Double> implements DoubleSupplier {
	protected DefaultedValue.Double value;
	protected DefaultedValue.Double remoteValue;
	public double minValue = -Double.MAX_VALUE;
	public double maxValue = Double.MAX_VALUE;

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

	@Override
	public double getAsDouble() {
		return getValueToUse(cfgState).value;
	}

	@Override
	public void set(Double value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = sanitize(value);
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
	public ConditionCheck isValueValid(Double value, Component optionName) {
		if (value < minValue) {
			return valueTooLow(optionName, minValue);
		}
		if (value > maxValue) {
			return valueTooHigh(optionName, maxValue);
		}
		return super.isValueValid(value, optionName);
	}
	
	@Override
	public Double sanitize(Double value) {
		return Mth.clamp(value, minValue, maxValue);
	}
	
	@Override
	public void setRemoteState() {
		remoteValue = new DefaultedValue.Double(getDefaultValue());
		super.setRemoteState();
	}
	
	@Override
	public void unsetRemoteState() {
		remoteValue = null;
		super.unsetRemoteState();
	}
	
	protected DefaultedValue.Double getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
