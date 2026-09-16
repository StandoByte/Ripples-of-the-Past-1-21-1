package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public class ConfigEnum<T extends Enum<T>> extends ConfigOption<T> {
	public final Class<T> enumClass;
	protected DefaultedValue<T> value;
	protected DefaultedValue<T> remoteValue;

	public ConfigEnum(Class<T> enumClass, T defaultValue) {
		super(CodecUtil.enumCodec(enumClass), NeoForgeStreamCodecs.enumCodec(enumClass));
		this.enumClass = enumClass;
		this.value = new DefaultedValue<>(defaultValue);
	}

	@Override
	public T get(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value;
	}
	
	@Override
	public void set(T value, CommonConfigState cfgState) {
		getValueToUse(cfgState).value = value;
	}
	
	@Override
	public boolean isDefault(CommonConfigState cfgState) {
		return getValueToUse(cfgState).value == getDefaultValue();
	}
	
	public T getDefaultValue() {
		return this.value.defaultValue;
	}

	@Override
	public void reset(CommonConfigState cfgState) {
		getValueToUse(cfgState).reset();
	}
	
	@Override
	public void setRemoteConfig() {
		super.setRemoteConfig();
		remoteValue = new DefaultedValue<T>(getDefaultValue());
	}
	
	@Override
	public void unsetRemoteConfig() {
		super.unsetRemoteConfig();
		remoteValue = null;
	}
	
	protected DefaultedValue<T> getValueToUse(CommonConfigState cfgState) {
		return switch (cfgState) {
			case LOCAL -> value;
			case REMOTE -> remoteValue;
		};
	}

}
