package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.functions.CodecUtil;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

public class ConfigEnum<T extends Enum<T>> extends ConfigOption<T> {
	public final Class<T> enumClass;
	protected DefaultedValue<T> value;

	public ConfigEnum(Class<T> enumClass, T defaultValue) {
		super(CodecUtil.enumCodec(enumClass), NeoForgeStreamCodecs.enumCodec(enumClass));
		this.enumClass = enumClass;
		this.value = new DefaultedValue<>(defaultValue);
	}

	@Override
	public T get() {
		return value.value;
	}
	
	@Override
	public void set(T value) {
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

}
