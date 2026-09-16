package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigBool extends ConfigOption<Boolean> {
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
	public void setRemoteConfig() {
		super.setRemoteConfig();
		remoteValue = new DefaultedValue.Bool(getDefaultValue());
	}
	
	@Override
	public void unsetRemoteConfig() {
		super.unsetRemoteConfig();
		remoteValue = null;
	}
	
	protected DefaultedValue.Bool getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
