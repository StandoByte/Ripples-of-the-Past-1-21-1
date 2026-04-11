package com.github.standobyte.jojo.subsystems.movement_input_sync;

import com.github.standobyte.jojo.PacketsRegister;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClPlayerMovementInputPacket(int entityId, float left, float forward, boolean jumping, boolean shift, boolean sprint) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<ClPlayerMovementInputPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ClPlayerMovementInputPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClPlayerMovementInputPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClPlayerMovementInputPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ClPlayerMovementInputPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, ClPlayerMovementInputPacket::entityId,
				ByteBufCodecs.FLOAT, ClPlayerMovementInputPacket::left,
				ByteBufCodecs.FLOAT, ClPlayerMovementInputPacket::forward,
				ByteBufCodecs.BOOL, ClPlayerMovementInputPacket::jumping,
				ByteBufCodecs.BOOL, ClPlayerMovementInputPacket::shift,
				ByteBufCodecs.BOOL, ClPlayerMovementInputPacket::sprint,
				ClPlayerMovementInputPacket::new);

		@Override
		public void handle(ClPlayerMovementInputPacket payload, IPayloadContext context) {
			Player player = context.player();
			PlayerMovementInputData.handleServerboundPacket(payload, player);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
