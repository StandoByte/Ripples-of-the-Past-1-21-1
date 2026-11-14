package com.github.standobyte.jojo.config.internal.packets;

import java.util.Collection;
import java.util.Map;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClCommonServerConfigEditPacket implements CustomPacketPayload {
	public ConfigId configId;
	public Map<String, ConfigOption<?>> writeFields;
	public RegistryFriendlyByteBuf read;
	
	public ClCommonServerConfigEditPacket(ConfigId configId, 
			Map<String, ConfigOption<?>> editedConfigOptions) {
		this.configId = configId;
		this.writeFields = editedConfigOptions;
	}
	
	protected ClCommonServerConfigEditPacket(ConfigId configId, 
			RegistryFriendlyByteBuf read) {
		this.configId = configId;
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
			ConfigId.STREAM_CODEC.encode(buf, packet.configId);
			
			NetworkUtil.writeCollection(buf, packet.writeFields.keySet(), FriendlyByteBuf::writeUtf);
			for (ConfigOption<?> field : packet.writeFields.values()) {
				field.toBuf(buf);
			}
		}

		@Override
		public ClCommonServerConfigEditPacket decode(RegistryFriendlyByteBuf buf) {
			ConfigId configId = ConfigId.STREAM_CODEC.decode(buf);
			RegistryFriendlyByteBuf read = NetworkUtil.extraPacketData(buf, buf.registryAccess());
			return new ClCommonServerConfigEditPacket(configId, read);
		}

		@Override
		public void handle(ClCommonServerConfigEditPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			
			if (payload.read != null) {
				RegistryFriendlyByteBuf buf = payload.read;
				Collection<String> fieldNames = NetworkUtil.readCollection(buf, FriendlyByteBuf::readUtf);
				
				ConfigNetworkFunctions.srvAcceptCommonSettingEdit(player, payload.configId, buf, fieldNames);
				payload.read.release();
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
