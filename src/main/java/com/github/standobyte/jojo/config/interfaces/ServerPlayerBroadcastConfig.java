package com.github.standobyte.jojo.config.interfaces;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public interface ServerPlayerBroadcastConfig<C2> {
	C2 getObj();
	
	boolean isDefault();
	void reset(Player player);
	void toBuf(RegistryFriendlyByteBuf buf);
	void fromBuf(RegistryFriendlyByteBuf buf, Player player);
}
