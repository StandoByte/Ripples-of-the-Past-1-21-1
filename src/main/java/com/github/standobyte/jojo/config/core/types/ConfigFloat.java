package com.github.standobyte.jojo.config.core.types;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.mojang.serialization.codecs.PrimitiveCodec;

import net.minecraft.network.codec.ByteBufCodecs;

public class ConfigFloat extends ConfigOption<Float> {
	protected DefaultedValue.Float value;

	public ConfigFloat(int defaultValue) {
		super(PrimitiveCodec.FLOAT, ByteBufCodecs.FLOAT);
		this.value = new DefaultedValue.Float(defaultValue);
	}

	@Override
	public Float get() {
		return value.value;
	}
	
	public float getAsFloat() {
		return value.value;
	}

	@Override
	public void set(Float value) {
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
