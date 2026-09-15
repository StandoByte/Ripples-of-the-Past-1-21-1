package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigFloat extends ConfigOption<Float> {
	protected DefaultedValue.Float value;
	protected float minValue = -Float.MAX_VALUE;
	protected float maxValue = Float.MAX_VALUE;

	public ConfigFloat(float defaultValue) {
		super(PrimitiveCodec.FLOAT, ByteBufCodecs.FLOAT);
		this.value = new DefaultedValue.Float(defaultValue);
	}
	
	public ConfigFloat initRange(float minValue, float maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		return this;
	}

	public ConfigFloat initMinValue(float minValue) { return initRange(minValue, this.maxValue); }
	public ConfigFloat initMaxValue(float maxValue) { return initRange(this.minValue, maxValue); }
	public ConfigFloat initNonNegative() { return initRange(0, this.maxValue); }

	@Override
	public Float get() {
		return value.value;
	}
	
	public float getAsFloat() {
		return value.value;
	}

	@Override
	public void set(Float value) {
		this.value.value = value;
	}
	
	@Override
	public boolean isDefault() {
		return this.value.defaultValue == this.value.value;
	}

	@Override
	public void reset() {
		this.value.reset();
	}
	
	@Override
	public ConfigFloat deepCopy() {
		ConfigFloat copy = new ConfigFloat(value.defaultValue);
		copy.value.value = this.value.value;
		copy.minValue = this.minValue;
		copy.maxValue = this.maxValue;
		return copy;
	}

}
