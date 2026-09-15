package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigDouble extends ConfigOption<Double> {
	protected DefaultedValue.Double value;
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
	public Double get() {
		return value.value;
	}

	public double getAsDouble() {
		return value.value;
	}

	@Override
	public void set(Double value) {
		this.value.value = Mth.clamp(value, minValue, maxValue);
	}

	@Override
	public boolean isDefault() {
		return this.value.defaultValue == this.value.value;
	}
	
	public double getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset() {
		this.value.reset();
	}
	
	@Override
	public ConfigDouble deepCopy() {
		ConfigDouble copy = new ConfigDouble(value.defaultValue);
		copy.value.value = this.value.value;
		copy.minValue = this.minValue;
		copy.maxValue = this.maxValue;
		return copy;
	}

}
