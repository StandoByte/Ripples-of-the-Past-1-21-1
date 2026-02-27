package com.github.standobyte.jojo.util.syncheddata;

import net.minecraft.world.level.Level;

public interface HasLevelReference {
	Level level();
	default boolean isClientSide() {
		return level().isClientSide();
	}
}
