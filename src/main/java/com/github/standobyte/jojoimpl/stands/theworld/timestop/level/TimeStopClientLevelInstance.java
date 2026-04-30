package com.github.standobyte.jojoimpl.stands.theworld.timestop.level;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

public record TimeStopClientLevelInstance(int id, ChunkPos center, int chunkRange) {
	
	public static void toBuf(RegistryFriendlyByteBuf buf, TimeStopEffect serverEffect) {
		buf.writeInt(serverEffect.getId());
		buf.writeChunkPos(serverEffect.initialPos);
	}
	
	public static TimeStopClientLevelInstance fromBuf(RegistryFriendlyByteBuf buf) {
		int id = buf.readInt();
		ChunkPos center = buf.readChunkPos();
		int chunkRange = TimeStopEffect.CHUNK_RANGE;
		return new TimeStopClientLevelInstance(id, center, chunkRange);
	}
	
}
