package com.github.standobyte.jojo.config.internal.packets;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PlayerBroadcastConfigPacket implements CustomPacketPayload {
	public UUID playerId;
	public ConfigId configId;
	public Optional<Consumer<RegistryFriendlyByteBuf>> write;
	public Optional<RegistryFriendlyByteBuf> read;
	
	public PlayerBroadcastConfigPacket(UUID playerId, ConfigId configId, Consumer<RegistryFriendlyByteBuf> write) {
		this.playerId = playerId;
		this.configId = configId;
		this.write = Optional.ofNullable(write);
	}
	
	public static PlayerBroadcastConfigPacket reset(UUID playerId, ConfigId configId) {
		return new PlayerBroadcastConfigPacket(playerId, configId, (Consumer<RegistryFriendlyByteBuf>) null);
	}
	
	protected PlayerBroadcastConfigPacket(UUID playerId, ConfigId configId, Optional<RegistryFriendlyByteBuf> read) {
		this.playerId = playerId;
		this.configId = configId;
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
			ConfigId.STREAM_CODEC.encode(buf, packet.configId);
			NetworkUtil.writeOptional(packet.write, buf, (buffer, writer) -> writer.accept(buffer));
		}

		@Override
		public PlayerBroadcastConfigPacket decode(RegistryFriendlyByteBuf buf) {
			UUID playerId = buf.readUUID();
			ConfigId configId = ConfigId.STREAM_CODEC.decode(buf);
			Optional<RegistryFriendlyByteBuf> read = NetworkUtil.readOptional(buf, 
					buffer -> NetworkUtil.extraPacketData(buffer, buffer.registryAccess()));
			return new PlayerBroadcastConfigPacket(playerId, configId, read);
		}

		@Override
		public void handle(PlayerBroadcastConfigPacket payload, IPayloadContext context) {
			ConfigNetworkFunctions.clAcceptAnotherPlayerBroadcastConfig(payload);
			if (payload.read != null) payload.read.ifPresent(ByteBuf::release);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
