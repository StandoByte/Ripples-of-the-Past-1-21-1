package com.github.standobyte.jojo.config.interfaces.fileimpl;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.interfaces.ServerPlayerBroadcastConfig;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;

public class ServerPlayerBroadcastObjectConfig<C2> implements ServerPlayerBroadcastConfig<C2> {
	@Nullable private final ObjectConfigHelper<C2> configState;

	public ServerPlayerBroadcastObjectConfig(ObjectConfigHelper<C2> config) {
		this.configState = config;
	}
	
	@Override
	public C2 getObj() {
		return configState.configObj;
	}
	
	@Override
	public boolean isDefault() {
		return configState.isDefault();
	}

	@Override
	public void reset() {
		configState.reset();
	}
	
	@Override
	public void toBuf(RegistryFriendlyByteBuf buf) {
		configState.toBuf(buf);
	}
	
	@Override
	public void fromBuf(RegistryFriendlyByteBuf buf) {
		configState.fromBuf(buf);
	}
	
}
