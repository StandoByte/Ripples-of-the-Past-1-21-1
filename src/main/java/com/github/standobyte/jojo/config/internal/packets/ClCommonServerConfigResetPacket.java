package com.github.standobyte.jojo.config.internal.packets;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClCommonServerConfigResetPacket implements CustomPacketPayload {
	public String modId;
	
	public ClCommonServerConfigResetPacket(String modId) {
		this.modId = modId;
	}
	
	
	private static CustomPacketPayload.Type<ClCommonServerConfigResetPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClCommonServerConfigResetPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClCommonServerConfigResetPacket> type() {
			return type;
		}

		@Override
		public void encode(ClCommonServerConfigResetPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeUtf(packet.modId);
		}

		@Override
		public ClCommonServerConfigResetPacket decode(RegistryFriendlyByteBuf buf) {
			String modId = buf.readUtf();
			return new ClCommonServerConfigResetPacket(modId);
		}

		@Override
		public void handle(ClCommonServerConfigResetPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			ConfigNetworkFunctions.srvAcceptCommonSettingsReset(player, payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
