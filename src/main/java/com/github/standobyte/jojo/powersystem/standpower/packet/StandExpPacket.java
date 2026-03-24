package com.github.standobyte.jojo.powersystem.standpower.packet;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandTypePersistentData;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StandExpPacket(float exp) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<StandExpPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<StandExpPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<StandExpPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, StandExpPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, StandExpPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT, StandExpPacket::exp,
				StandExpPacket::new);

		@Override
		public void handle(StandExpPacket payload, IPayloadContext context) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null) {
				StandTypePersistentData data = standPower.getCurTypeData();
				if (data != null) {
					data.setExp(payload.exp, standPower, true);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
