package com.github.standobyte.jojo.config;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;

public interface ModConfigInterface<C1, C2, C3> {
	C1 getClient();
	C2 getPlayerBroadcast(@Nullable Player player);
	C3 getCommon();
	
	void saveClient();
	void sendClientBroadcast();
	void saveCommon();
	
	String modId();
}
