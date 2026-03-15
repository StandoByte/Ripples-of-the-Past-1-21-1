package com.github.standobyte.jojo.util.functions_network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public class PacketDistributor2 {

	public static void sendToPlayersTrackingEntity(Entity entity, Predicate<ServerPlayer> filter, boolean sendToSelf, 
			CustomPacketPayload payload, CustomPacketPayload... payloads) {
		if (entity.level().isClientSide()) {
			throw new IllegalStateException("Cannot send clientbound payloads on the client");
		} else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
			Packet<?> packet = makeClientboundPacket(payload, payloads);

			ChunkMap.TrackedEntity trackedEntity = chunkCache.chunkMap.entityMap.get(entity.getId());
			if (trackedEntity != null) {
				for (ServerPlayerConnection player : trackedEntity.seenBy) {
					if (filter.test(player.getPlayer())) {
						player.send(packet);
					}
				}
				if (sendToSelf && entity instanceof ServerPlayer player && filter.test(player)) {
					player.connection.send(packet);
				}
			}
		}
	}
	
	public static void sendToPlayers(Entity entity, Stream<ServerPlayer> players, boolean sendToSelf, 
			CustomPacketPayload payload, CustomPacketPayload... payloads) {
		if (entity.level().isClientSide()) {
			throw new IllegalStateException("Cannot send clientbound payloads on the client");
		} else {
			Packet<?> packet = makeClientboundPacket(payload, payloads);

			players.forEach(player -> player.connection.send(packet));
			if (sendToSelf && entity instanceof ServerPlayer player) {
				player.connection.send(packet);
			}
		}
	}
	
	/**
	 * Copy-paste of the private {@link net.neoforged.neoforge.network.PacketDistributor#makeClientboundPacket} method.
	 */
	public static Packet<?> makeClientboundPacket(CustomPacketPayload payload, CustomPacketPayload... payloads) {
		if (payloads.length > 0) {
			final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
			packets.add(new ClientboundCustomPayloadPacket(payload));
			for (CustomPacketPayload otherPayload : payloads) {
				packets.add(new ClientboundCustomPayloadPacket(otherPayload));
			}
			return new ClientboundBundlePacket(packets);
		} else {
			return new ClientboundCustomPayloadPacket(payload);
		}
	}
	
}
