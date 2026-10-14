package com.github.standobyte.jojo.config.interfaces;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface ServerPlayerBroadcastConfig<C2> {
	C2 getObj();
	
	boolean isDefault();
	void reset();
	void toBuf(RegistryFriendlyByteBuf buf);
	void fromBuf(RegistryFriendlyByteBuf buf);
}
