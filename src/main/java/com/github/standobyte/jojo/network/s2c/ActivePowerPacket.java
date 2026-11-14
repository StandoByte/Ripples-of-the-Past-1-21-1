package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivePowerPacket(int entityId, Optional<PowerClass<?>> powerClass) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ActivePowerPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ActivePowerPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ActivePowerPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ActivePowerPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<ByteBuf, ActivePowerPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, ActivePowerPacket::entityId,
				PowerClass.NETWORK_CODEC.apply(ByteBufCodecs::optional), ActivePowerPacket::powerClass, 
				ActivePowerPacket::new);

		@Override
		public void handle(ActivePowerPacket payload, IPayloadContext context) {
			if (ClientProxy.getEntityById(payload.entityId) instanceof LivingEntity living) {
				Power.setPowersActive(living, payload.powerClass);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
}
