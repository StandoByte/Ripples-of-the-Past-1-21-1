package com.github.standobyte.jojo.config.core.types;

import java.util.function.BooleanSupplier;

import com.github.standobyte.jojo.config.BoolOrPlayerPref;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigBool extends ConfigOption<Boolean> implements BooleanSupplier {
	protected DefaultedValue.Bool value;
	protected DefaultedValue.Bool remoteValue;

	public ConfigBool(boolean defaultValue) {
		super(PrimitiveCodec.BOOL, ByteBufCodecs.BOOL);
		this.value = new DefaultedValue.Bool(defaultValue);
	}

	@Override
	public Boolean get(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value;
	}
	
	@Override
	public boolean getAsBoolean() {
		return getValueToUse(cfgState).value;
	}

	@Override
	public void set(Boolean value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = value;
	}
	
	@Override
	public boolean isDefault(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value == getDefaultValue();
	}
	
	public boolean getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset(CommonConfigState cfgState) {
		getValueToUse(cfgState).reset();
	}
	
	@Override
	public void setRemoteState() {
		super.setRemoteState();
		remoteValue = new DefaultedValue.Bool(getDefaultValue());
	}
	
	@Override
	public void unsetRemoteState() {
		super.unsetRemoteState();
		remoteValue = null;
	}
	
	protected DefaultedValue.Bool getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}
	
	
	public MutableComponent makeButtonMessage(Component optionName, Boolean value) {
		return BoolOrPlayerPref.optionStatus(optionName, value);
	}

}
