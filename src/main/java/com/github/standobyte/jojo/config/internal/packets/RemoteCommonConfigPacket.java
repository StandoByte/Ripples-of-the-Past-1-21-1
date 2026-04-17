package com.github.standobyte.jojo.config.internal.packets;

import java.util.function.Consumer;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RemoteCommonConfigPacket implements CustomPacketPayload {
	public String modId;
	public Consumer<RegistryFriendlyByteBuf> write;
	public RegistryFriendlyByteBuf read;
	
	public RemoteCommonConfigPacket(String modId, Consumer<RegistryFriendlyByteBuf> write) {
		this.modId = modId;
		this.write = write;
	}
	
	protected RemoteCommonConfigPacket(String modId, RegistryFriendlyByteBuf read) {
		this.modId = modId;
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
			buf.writeUtf(packet.modId);
			packet.write.accept(buf);
		}

		@Override
		public RemoteCommonConfigPacket decode(RegistryFriendlyByteBuf buf) {
			String modId = buf.readUtf();
			RegistryFriendlyByteBuf read = NetworkUtil.extraPacketData(buf, buf.registryAccess());
			return new RemoteCommonConfigPacket(modId, read);
		}

		@Override
		public void handle(RemoteCommonConfigPacket payload, IPayloadContext context) {
			ConfigNetworkFunctions.clAcceptCommonConfigState(payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
