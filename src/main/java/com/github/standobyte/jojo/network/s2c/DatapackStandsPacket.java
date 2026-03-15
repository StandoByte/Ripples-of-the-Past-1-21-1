package com.github.standobyte.jojo.network.s2c;

import java.util.Collection;
import java.util.Map;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DatapackStandsPacket(Collection<Map.Entry<ResourceLocation, JsonObject>> standData) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<DatapackStandsPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<DatapackStandsPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<DatapackStandsPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, DatapackStandsPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		private static final StreamCodec<FriendlyByteBuf, Map.Entry<ResourceLocation, JsonObject>> ENTRY_CODEC = StreamCodec.composite(
						ResourceLocation.STREAM_CODEC, Map.Entry::getKey, 
						NetworkUtil.JSON_OBJECT_CODEC, Map.Entry::getValue, 
						Map::entry);
		
		public static final StreamCodec<FriendlyByteBuf, DatapackStandsPacket> STREAM_CODEC = NetworkUtil.collectionCodec(ENTRY_CODEC)
				.map(DatapackStandsPacket::new, DatapackStandsPacket::standData);

		@Override
		public void handle(DatapackStandsPacket payload, IPayloadContext context) {
			DataDrivenStandsLoader.receivePacket(payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
