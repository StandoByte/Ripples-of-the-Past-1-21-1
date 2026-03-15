package com.github.standobyte.jojo.subsystems.itemtracking;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrackedItemPacket implements CustomPacketPayload {
	private final UUID trackerId;
	private final ItemStack itemStack;
	@Nullable private final String context;
	private final OptionalInt entityId;
	private final Optional<BlockPos> blockPos;

	public TrackedItemPacket(UUID trackerId, ItemStack itemStack, @Nullable String context, 
			OptionalInt entityId, Optional<BlockPos> blockPos) {
		this.trackerId = trackerId;
		this.itemStack = itemStack;
		this.context = context;
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
			NetworkUtil.writeOptionally(packet.itemStack, buf, ItemStack.OPTIONAL_STREAM_CODEC);
			NetworkUtil.writeOptionally(packet.context, buf, ByteBufCodecs.STRING_UTF8);
			NetworkUtil.writeOptionalInt(buf, packet.entityId, false);
			NetworkUtil.writeOptional(packet.blockPos, buf, BlockPos.STREAM_CODEC);
		}

		@Override
		public TrackedItemPacket decode(RegistryFriendlyByteBuf buf) {
			TrackedItemPacket packet = new TrackedItemPacket(
					buf.readUUID(), 
					NetworkUtil.readOptional(buf, ItemStack.OPTIONAL_STREAM_CODEC).orElse(null),
					NetworkUtil.readOptional(buf, ByteBufCodecs.STRING_UTF8).orElse(null),
					NetworkUtil.readOptionalInt(buf, false),
					NetworkUtil.readOptional(buf, BlockPos.STREAM_CODEC));
			return packet;
		}

		@Override
		public void handle(TrackedItemPacket payload, IPayloadContext context) {
			ItemTracking trackerMap = ClientProxy.clientTrackedItems;
			if (payload.entityId.isPresent()) {
				ItemTracker tracker = trackerMap.clComputeIfAbsent(payload.trackerId);
				tracker.context = payload.context;
				tracker.setAtEntity(payload.itemStack, payload.entityId.getAsInt(), ClientProxy.getClientWorld(), null, null);
			}
			else if (payload.blockPos.isPresent()) {
				ItemTracker tracker = trackerMap.clComputeIfAbsent(payload.trackerId);
				tracker.context = payload.context;
				tracker.setAtBlockPos(payload.itemStack, payload.blockPos.get(), ClientProxy.getClientWorld(), null, null);
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
