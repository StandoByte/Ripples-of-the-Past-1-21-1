package com.github.standobyte.jojo.config.core;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class ConfigOption<T> implements Supplier<T> {
	public abstract void set(T value);
	public abstract boolean isDefault();
	public abstract void reset();


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
