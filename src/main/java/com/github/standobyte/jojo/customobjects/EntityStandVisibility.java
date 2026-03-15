package com.github.standobyte.jojo.customobjects;

import com.github.standobyte.jojo.client.ClientGlobals;

import net.minecraft.world.level.Level;

public interface EntityStandVisibility {
	boolean onlyVisibleToStandUsers();
	
	default boolean clientCantSeeThisStand() {
		return onlyVisibleToStandUsers() && level().isClientSide() && !ClientGlobals.canSeeStands;
	}
	
	Level level();
}
