package com.github.standobyte.jojo.adventure.tmp_charactertest;

import java.util.Optional;
import java.util.UUID;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClTakeGroupMemberControlPacket(Optional<UUID> entityId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClTakeGroupMemberControlPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClTakeGroupMemberControlPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClTakeGroupMemberControlPacket> type() {
			return type;
		}

		@Override
		public void encode(ClTakeGroupMemberControlPacket packet, RegistryFriendlyByteBuf buf) {
			NetworkUtil.writeOptional(packet.entityId, buf, (buffer, uuid) -> buffer.writeUUID(uuid));
		}

		@Override
		public ClTakeGroupMemberControlPacket decode(RegistryFriendlyByteBuf buf) {
			Optional<UUID> entityId = NetworkUtil.readOptional(buf, buffer -> buffer.readUUID());
			return new ClTakeGroupMemberControlPacket(entityId);
		}

		@Override
		public void handle(ClTakeGroupMemberControlPacket payload, IPayloadContext context) {
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
