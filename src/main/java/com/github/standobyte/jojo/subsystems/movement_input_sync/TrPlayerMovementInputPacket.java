package com.github.standobyte.jojo.subsystems.movement_input_sync;

import com.github.standobyte.jojo.PacketsRegister;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrPlayerMovementInputPacket(int entityId, float left, float forward, boolean jumping, boolean shift, boolean sprint) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<TrPlayerMovementInputPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrPlayerMovementInputPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrPlayerMovementInputPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrPlayerMovementInputPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrPlayerMovementInputPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrPlayerMovementInputPacket::entityId,
				ByteBufCodecs.FLOAT, TrPlayerMovementInputPacket::left,
				ByteBufCodecs.FLOAT, TrPlayerMovementInputPacket::forward,
				ByteBufCodecs.BOOL, TrPlayerMovementInputPacket::jumping,
				ByteBufCodecs.BOOL, TrPlayerMovementInputPacket::shift,
				ByteBufCodecs.BOOL, TrPlayerMovementInputPacket::sprint,
				TrPlayerMovementInputPacket::new);

		@Override
		public void handle(TrPlayerMovementInputPacket payload, IPayloadContext context) {
			PlayerMovementInputData.handleTrackingClientboundPacket(payload);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
