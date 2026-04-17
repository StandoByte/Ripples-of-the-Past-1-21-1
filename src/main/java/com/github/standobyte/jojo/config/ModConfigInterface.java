package com.github.standobyte.jojo.config;

import net.minecraft.world.entity.player.Player;

public interface ModConfigInterface<C1, C2, C3> {
	C1 getClient();
	C2 getPlayerBroadcast(Player player);
	C3 getCommon();
}
