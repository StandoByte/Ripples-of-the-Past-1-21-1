package com.github.standobyte.jojo.config.internal.packets;

import java.util.function.Consumer;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RemoteCommonConfigPacket implements CustomPacketPayload {
	public ConfigId configId;
	public Consumer<RegistryFriendlyByteBuf> write;
	public RegistryFriendlyByteBuf read;
	
	public RemoteCommonConfigPacket(ConfigId configId, Consumer<RegistryFriendlyByteBuf> write) {
		this.configId = configId;
		this.write = write;
	}
	
	protected RemoteCommonConfigPacket(ConfigId configId, RegistryFriendlyByteBuf read) {
		this.configId = configId;
		this.read = read;
	}
	
	
	private static CustomPacketPayload.Type<RemoteCommonConfigPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<RemoteCommonConfigPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<RemoteCommonConfigPacket> type() {
			return type;
		}

		@Override
		public void encode(RemoteCommonConfigPacket packet, RegistryFriendlyByteBuf buf) {
			ConfigId.STREAM_CODEC.encode(buf, packet.configId);
			packet.write.accept(buf);
		}

		@Override
		public RemoteCommonConfigPacket decode(RegistryFriendlyByteBuf buf) {
			ConfigId configId = ConfigId.STREAM_CODEC.decode(buf);
			RegistryFriendlyByteBuf read = NetworkUtil.extraPacketData(buf, buf.registryAccess());
			return new RemoteCommonConfigPacket(configId, read);
		}

		@Override
		public void handle(RemoteCommonConfigPacket payload, IPayloadContext context) {
			ConfigNetworkFunctions.clAcceptCommonConfigState(payload);
			if (payload.read != null) payload.read.release();
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
