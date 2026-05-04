package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public interface TimeStopInstance {
	ChunkPos center();
	int chunkRange();
	
	default boolean isInRange(BlockPos blockPos) {
		ChunkPos center = center();
		return isInRange(center.x, center.z, 
				SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), 
				chunkRange());
	}
	
	default boolean isInRange(ChunkPos chunkPos) {
		ChunkPos center = center();
		return isInRange(center.x, center.z, 
				chunkPos.x, chunkPos.z, 
				chunkRange());
	}
	
	default boolean isInRange(long chunkPosPacked) {
		return isInRange(new ChunkPos(chunkPosPacked));
	}
	
	public static boolean isInRange(int x1, int z1, int x2, int z2, int range) {
		return range <= 0 || Math.abs(x1 - x2) < range && Math.abs(z1 - z2) < range;
	}
	
}
