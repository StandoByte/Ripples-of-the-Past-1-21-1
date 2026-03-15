package com.github.standobyte.jojo.mechanics.resolve;

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

public record TrResolvePacket(int entityId, float resolve) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrResolvePacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<TrResolvePacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrResolvePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrResolvePacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, TrResolvePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrResolvePacket::entityId,
				ByteBufCodecs.FLOAT, TrResolvePacket::resolve,
				TrResolvePacket::new);

		@Override
		public void handle(TrResolvePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				StandPower standPower = StandPower.get(living);
				if (standPower != null) standPower.resolveHandler.setResolveValue(standPower, payload.resolve);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
