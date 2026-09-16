package com.github.standobyte.jojo.config.core;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ConfigOption<T> implements Supplier<T> {
	protected CommonConfigState cfgState = CommonConfigState.LOCAL;
	
	@Override
	public T get() {
		return get(this.cfgState);
	}
	
	public void set(T value) {
		set(value, this.cfgState);
	}
	
	public boolean isDefault() {
		return isDefault(this.cfgState);
	}
	
	public void reset() {
		reset(this.cfgState);
	}
	
	public void setRemoteConfig() {
		this.cfgState = CommonConfigState.REMOTE;
	}
	
	public void unsetRemoteConfig() {
		this.cfgState = CommonConfigState.LOCAL;
	}
	
	protected abstract T get(CommonConfigState cfgState);
	protected abstract void set(T value, CommonConfigState cfgState);
	protected abstract boolean isDefault(CommonConfigState cfgState);
	protected abstract void reset(CommonConfigState cfgState);


	@ApiStatus.Internal
	public void init(String fieldName) {
		this.fieldName = fieldName;
	}

	@ApiStatus.Internal
	public String getFieldName() {
		return fieldName;
	}

	protected String fieldName;
	protected Codec<T> jsonCodec;
	protected StreamCodec<? super RegistryFriendlyByteBuf, T> networkCodec;

	public ConfigOption(Codec<T> jsonCodec, StreamCodec<? super RegistryFriendlyByteBuf, T> networkCodec) {
		this.jsonCodec = jsonCodec;
		this.networkCodec = networkCodec;
	}

	public JsonElement toJson(String fieldName) {
		return jsonCodec.encodeStart(JsonOps.INSTANCE, get())
				.ifError(error -> {
					JojoMod.getLogger().error("Failed to write config option {}: {}", fieldName, error.message());
				}).result().orElse(null);
	}

	public void fromJson(JsonElement json, String fieldName) {
		jsonCodec.decode(JsonOps.INSTANCE, json)
				.ifSuccess(result -> this.set(result.getFirst()))
				.ifError(error -> {
					JojoMod.getLogger().error("Failed to read config option {}: {}", fieldName, error.message());
					this.reset();
				});
	}

	public void toBuf(RegistryFriendlyByteBuf buf) {
		networkCodec.encode(buf, get());
	}

	public void fromBuf(RegistryFriendlyByteBuf buf) {
		set(networkCodec.decode(buf));
	}
}
