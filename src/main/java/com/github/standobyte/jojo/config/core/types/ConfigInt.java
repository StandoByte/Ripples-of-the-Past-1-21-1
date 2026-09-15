package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigInt extends ConfigOption<Integer> {
	protected DefaultedValue.Int value;
	protected int minValue = Integer.MIN_VALUE;
	protected int maxValue = Integer.MAX_VALUE;

	public ConfigInt(int defaultValue) {
		super(PrimitiveCodec.INT, ByteBufCodecs.INT);
		this.value = new DefaultedValue.Int(defaultValue);
	}
	
	public ConfigInt initRange(int minValue, int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		return this;
	}

	public ConfigInt initMinValue(int minValue) { return initRange(minValue, this.maxValue); }
	public ConfigInt initMaxValue(int maxValue) { return initRange(this.minValue, maxValue); }
	public ConfigInt initNonNegative() { return initRange(0, this.maxValue); }
	

	@Override
	public Integer get() {
		return value.value;
	}
	
	public int getAsInt() {
		return value.value;
	}

	@Override
	public void set(Integer value) {
		this.value.value = Mth.clamp(value, minValue, maxValue);
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
	public ConfigInt deepCopy() {
		ConfigInt copy = new ConfigInt(value.defaultValue);
		copy.value.value = this.value.value;
		copy.minValue = this.minValue;
		copy.maxValue = this.maxValue;
		return copy;
	}

}
