package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface EntityMadeFromBlock {
	boolean crazyDRestore(BlockPos blockPos);
	default boolean isEntityAlive() {
		return ((Entity) this).isAlive();
	}
}
