package com.github.standobyte.jojo.mechanics.itemtracking.internal;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrackedItemPacket implements CustomPacketPayload {
	private final UUID trackerId;
	private final ItemStack itemStack;
	private final OptionalInt entityId;
	private final Optional<BlockPos> blockPos;

	public TrackedItemPacket(UUID trackerId, ItemStack itemStack, OptionalInt entityId, Optional<BlockPos> blockPos) {
		this.trackerId = trackerId;
		this.itemStack = itemStack;
		this.entityId = entityId;
		this.blockPos = blockPos;
	}

//    public static TrackedItemPacket entity(UUID trackerId, int entityId) {
//    	return new TrackedItemPacket(trackerId, OptionalInt.of(entityId), Optional.empty());
//    }
//
//    public static TrackedItemPacket blockPos(UUID trackerId, BlockPos blockPos) {
//    	return new TrackedItemPacket(trackerId, OptionalInt.empty(), Optional.of(blockPos));
//    }
//
//    public static TrackedItemPacket unknown(UUID trackerId) {
//    	return new TrackedItemPacket(trackerId, OptionalInt.empty(), Optional.empty());
//    }



	private static CustomPacketPayload.Type<TrackedItemPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrackedItemPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrackedItemPacket> type() {
			return type;
		}

		@Override
		public void encode(TrackedItemPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeUUID(packet.trackerId);
			NetworkUtil.writeOptionally(packet.itemStack, buf, ItemStack.STREAM_CODEC);
			NetworkUtil.writeOptionalInt(buf, packet.entityId, false);
			NetworkUtil.writeOptional(packet.blockPos, buf, BlockPos.STREAM_CODEC);
		}

		@Override
		public TrackedItemPacket decode(RegistryFriendlyByteBuf buf) {
			TrackedItemPacket packet = new TrackedItemPacket(
					buf.readUUID(), 
					NetworkUtil.readOptional(buf, ItemStack.STREAM_CODEC).orElse(null),
					NetworkUtil.readOptionalInt(buf, false),
					NetworkUtil.readOptional(buf, BlockPos.STREAM_CODEC));
			return packet;
		}

		@Override
		public void handle(TrackedItemPacket payload, IPayloadContext context) {
			ItemTracking trackerMap = ClientProxy.clientTrackedItems;
			if (payload.entityId.isPresent()) {
				ItemTracker tracker = trackerMap.clComputeIfAbsent(payload.trackerId);
				tracker.setAtEntity(payload.itemStack, payload.entityId.getAsInt(), ClientProxy.getClientWorld(), null);
			}
			else if (payload.blockPos.isPresent()) {
				ItemTracker tracker = trackerMap.clComputeIfAbsent(payload.trackerId);
				tracker.setAtBlockPos(payload.itemStack, payload.blockPos.get(), ClientProxy.getClientWorld(), null);
			}
			else {
				trackerMap.stopTracking(payload.trackerId, null);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
