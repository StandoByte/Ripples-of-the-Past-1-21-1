package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigInt extends ConfigOption<Integer> {
	protected DefaultedValue.Int value;

	public ConfigInt(int defaultValue) {
		super(PrimitiveCodec.INT, ByteBufCodecs.INT);
		this.value = new DefaultedValue.Int(defaultValue);
	}

	@Override
	public Integer get() {
		return value.value;
	}
	
	public int getAsInt() {
		return value.value;
	}

	@Override
	public void set(Integer value) {
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
