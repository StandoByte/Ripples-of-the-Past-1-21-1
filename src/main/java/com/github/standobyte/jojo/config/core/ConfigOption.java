package com.github.standobyte.jojo.config.core;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
	
	
	public static ConditionCheck valueTooLow(Component optionName, Object minValue) {
		return ConditionCheck.createNegative(Component.translatable("jojo_ripples.config.value_too_low", optionName, minValue));
	}
	
	public static ConditionCheck valueTooHigh(Component optionName, Object maxValue) {
		return ConditionCheck.createNegative(Component.translatable("jojo_ripples.config.value_too_high", optionName, maxValue));
	}
	
	public ConditionCheck isValueValid(T value, Component optionName) {
		return ConditionCheck.POSITIVE;
	}
	
	public T sanitize(T value) {
		return value;
	}
	
	
	public void setRemoteState() {
		this.cfgState = CommonConfigState.REMOTE;
	}
	
	public void unsetRemoteState() {
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
	public final Codec<T> jsonCodec;
	public final StreamCodec<? super RegistryFriendlyByteBuf, T> networkCodec;

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
