package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.github.standobyte.jojo.util.objects_java.OptionalFloat.FloatSupplier;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigFloat extends ConfigOption<Float> implements FloatSupplier {
	protected DefaultedValue.Float value;
	protected DefaultedValue.Float remoteValue;
	public float minValue = -Float.MAX_VALUE;
	public float maxValue = Float.MAX_VALUE;

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
		getValueToUse(cfgState).value = sanitize(value);
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
	public ConditionCheck isValueValid(Float value, Component optionName) {
		if (value < minValue) {
			return valueTooLow(optionName, minValue);
		}
		if (value > maxValue) {
			return valueTooHigh(optionName, maxValue);
		}
		return super.isValueValid(value, optionName);
	}
	
	@Override
	public Float sanitize(Float value) {
		return Mth.clamp(value, minValue, maxValue);
	}
	
	@Override
	public void setRemoteState() {
		remoteValue = new DefaultedValue.Float(getDefaultValue());
		super.setRemoteState();
	}
	
	@Override
	public void unsetRemoteState() {
		remoteValue = null;
		super.unsetRemoteState();
	}
	
	protected DefaultedValue.Float getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
