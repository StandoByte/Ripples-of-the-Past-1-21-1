package com.github.standobyte.jojo.config.internal.packets;

import java.util.function.Consumer;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClCommonServerConfigEditPacket implements CustomPacketPayload {
	public String modId;
	public String fieldName;
	public Consumer<RegistryFriendlyByteBuf> write;
	public RegistryFriendlyByteBuf read;
	
	public ClCommonServerConfigEditPacket(String modId, String fieldName, 
			Consumer<RegistryFriendlyByteBuf> writeField) {
		this.modId = modId;
		this.fieldName = fieldName;
		this.write = writeField;
	}
	
	protected ClCommonServerConfigEditPacket(String modId, String fieldName, 
			RegistryFriendlyByteBuf read) {
		this.modId = modId;
		this.fieldName = fieldName;
		this.read = read;
	}
	
	
	private static CustomPacketPayload.Type<ClCommonServerConfigEditPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClCommonServerConfigEditPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClCommonServerConfigEditPacket> type() {
			return type;
		}

		@Override
		public void encode(ClCommonServerConfigEditPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeUtf(packet.modId);
			buf.writeUtf(packet.fieldName);
			packet.write.accept(buf);
		}

		@Override
		public ClCommonServerConfigEditPacket decode(RegistryFriendlyByteBuf buf) {
			String modId = buf.readUtf();
			String fieldName = buf.readUtf();
			RegistryFriendlyByteBuf read = NetworkUtil.extraPacketData(buf, buf.registryAccess());
			return new ClCommonServerConfigEditPacket(modId, fieldName, read);
		}

		@Override
		public void handle(ClCommonServerConfigEditPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			ConfigNetworkFunctions.srvAcceptCommonSettingEdit(player, payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
