package com.github.standobyte.jojo.powersystem.standpower.packet;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrStaminaPacket(int entityId, float stamina) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrStaminaPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<TrStaminaPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrStaminaPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrStaminaPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, TrStaminaPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrStaminaPacket::entityId,
				ByteBufCodecs.FLOAT, TrStaminaPacket::stamina,
				TrStaminaPacket::new);

		@Override
		public void handle(TrStaminaPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				StandPower standPower = StandPower.get(living);
				if (standPower != null) standPower.setStamina(payload.stamina);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
