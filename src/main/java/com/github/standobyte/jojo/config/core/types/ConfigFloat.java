package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.github.standobyte.jojo.util.objects_java.OptionalFloat.FloatSupplier;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigFloat extends ConfigOption<Float> implements FloatSupplier {
	protected DefaultedValue.Float value;
	protected DefaultedValue.Float remoteValue;
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
	public Float get(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value;
	}
	
	@Override
	public float getAsFloat() {
		return getValueToUse(cfgState).value;
	}

	@Override
	public void set(Float value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = Mth.clamp(value, minValue, maxValue);
	}
	
	@Override
	public boolean isDefault(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value == getDefaultValue();
	}
	
	public float getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset(CommonConfigState cfgState) {
		getValueToUse(cfgState).reset();
	}
	
	@Override
	public void setRemoteState() {
		super.setRemoteState();
		remoteValue = new DefaultedValue.Float(getDefaultValue());
	}
	
	@Override
	public void unsetRemoteState() {
		super.unsetRemoteState();
		remoteValue = null;
	}
	
	protected DefaultedValue.Float getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
