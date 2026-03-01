package com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface BlockBreaking {
	float getProgress();
	boolean setAndSyncProgress(float progress, BlockPos blockPos, ServerLevel level);
}
