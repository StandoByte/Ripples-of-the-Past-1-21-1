package com.github.standobyte.jojo.network.c2s;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClActivePowerPacket(Optional<PowerClass<?>> powerClass) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClActivePowerPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ClActivePowerPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClActivePowerPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClActivePowerPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<ByteBuf, ClActivePowerPacket> STREAM_CODEC = StreamCodec.composite(
				PowerClass.NETWORK_CODEC.apply(ByteBufCodecs::optional), ClActivePowerPacket::powerClass, 
				ClActivePowerPacket::new);

		@Override
		public void handle(ClActivePowerPacket payload, IPayloadContext context) {
			Player player = context.player();
			Power.setPowersActive(player, payload.powerClass);
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
}
