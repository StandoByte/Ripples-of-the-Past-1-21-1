package com.github.standobyte.jojo.config.internal.packets;

import java.util.Optional;
import java.util.UUID;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RemoteHostUUIDConfigPacket implements CustomPacketPayload {
	public Optional<UUID> hostUUID;
	
	public RemoteHostUUIDConfigPacket(Optional<UUID> hostUUID) {
		this.hostUUID = hostUUID;
	}
	
	
	private static CustomPacketPayload.Type<RemoteHostUUIDConfigPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<RemoteHostUUIDConfigPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<RemoteHostUUIDConfigPacket> type() {
			return type;
		}

		@Override
		public void encode(RemoteHostUUIDConfigPacket packet, RegistryFriendlyByteBuf buf) {
			NetworkUtil.writeOptional(packet.hostUUID, buf, UUIDUtil.STREAM_CODEC);
		}

		@Override
		public RemoteHostUUIDConfigPacket decode(RegistryFriendlyByteBuf buf) {
			Optional<UUID> hostUUID = NetworkUtil.readOptional(buf, UUIDUtil.STREAM_CODEC);
			return new RemoteHostUUIDConfigPacket(hostUUID);
		}

		@Override
		public void handle(RemoteHostUUIDConfigPacket payload, IPayloadContext context) {
			ConfigNetworkFunctions.clAcceptServerHostUUID(payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
