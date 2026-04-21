package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigBool extends ConfigOption<Boolean> {
	protected DefaultedValue.Bool value;

	public ConfigBool(boolean defaultValue) {
		super(PrimitiveCodec.BOOL, ByteBufCodecs.BOOL);
		this.value = new DefaultedValue.Bool(defaultValue);
	}

	@Override
	public Boolean get() {
		return value.value;
	}
	
	public boolean getAsBoolean() {
		return value.value;
	}

	@Override
	public void set(Boolean value) {
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
