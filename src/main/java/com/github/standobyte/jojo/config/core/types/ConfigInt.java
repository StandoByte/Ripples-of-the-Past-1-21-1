package com.github.standobyte.jojo.config.core.types;

import java.util.function.IntSupplier;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Mth;

public class ConfigInt extends ConfigOption<Integer> implements IntSupplier {
	protected DefaultedValue.Int value;
	protected DefaultedValue.Int remoteValue;
	public int minValue = Integer.MIN_VALUE;
	public int maxValue = Integer.MAX_VALUE;

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
	public Integer get(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value;
	}
	
	@Override
	public int getAsInt() {
		return getValueToUse(cfgState).value;
	}

	@Override
	public void set(Integer value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = sanitize(value);
	}
	
	@Override
	public boolean isDefault(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value == getDefaultValue();
	}
	
	public int getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset(CommonConfigState cfgState) {
		getValueToUse(cfgState).reset();
	}
	
	@Override
	public ConditionCheck isValueValid(Integer value, Component optionName) {
		if (value < minValue) {
			return valueTooLow(optionName, minValue);
		}
		if (value > maxValue) {
			return valueTooHigh(optionName, maxValue);
		}
		return super.isValueValid(value, optionName);
	}
	
	@Override
	public Integer sanitize(Integer value) {
		return Mth.clamp(value, minValue, maxValue);
	}
	
	@Override
	public void setRemoteState() {
		remoteValue = new DefaultedValue.Int(getDefaultValue());
		super.setRemoteState();
	}
	
	@Override
	public void unsetRemoteState() {
		remoteValue = null;
		super.unsetRemoteState();
	}
	
	protected DefaultedValue.Int getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
