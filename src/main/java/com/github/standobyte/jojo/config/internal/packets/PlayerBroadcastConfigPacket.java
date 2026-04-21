package com.github.standobyte.jojo.config.internal.packets;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerBroadcastConfigPacket implements CustomPacketPayload {
	public UUID playerId;
	public String modId;
	public Optional<Consumer<RegistryFriendlyByteBuf>> write;
	public Optional<RegistryFriendlyByteBuf> read;
	
	public PlayerBroadcastConfigPacket(UUID playerId, String modId, Consumer<RegistryFriendlyByteBuf> write) {
		this.playerId = playerId;
		this.modId = modId;
		this.write = Optional.ofNullable(write);
	}
	
	public static PlayerBroadcastConfigPacket reset(UUID playerId, String modId) {
		return new PlayerBroadcastConfigPacket(playerId, modId, (Consumer<RegistryFriendlyByteBuf>) null);
	}
	
	protected PlayerBroadcastConfigPacket(UUID playerId, String modId, Optional<RegistryFriendlyByteBuf> read) {
		this.playerId = playerId;
		this.modId = modId;
		this.read = read;
	}
	
	
	private static CustomPacketPayload.Type<PlayerBroadcastConfigPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<PlayerBroadcastConfigPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<PlayerBroadcastConfigPacket> type() {
			return type;
		}

		@Override
		public void encode(PlayerBroadcastConfigPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeUUID(packet.playerId);
			buf.writeUtf(packet.modId);
			NetworkUtil.writeOptional(packet.write, buf, (buffer, writer) -> writer.accept(buffer));
		}

		@Override
		public PlayerBroadcastConfigPacket decode(RegistryFriendlyByteBuf buf) {
			UUID playerId = buf.readUUID();
			String modId = buf.readUtf();
			Optional<RegistryFriendlyByteBuf> read = NetworkUtil.readOptional(buf, 
					buffer -> NetworkUtil.extraPacketData(buffer, buffer.registryAccess()));
			return new PlayerBroadcastConfigPacket(playerId, modId, read);
		}

		@Override
		public void handle(PlayerBroadcastConfigPacket payload, IPayloadContext context) {
			ConfigNetworkFunctions.clAcceptAnotherPlayerBroadcastConfig(payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
