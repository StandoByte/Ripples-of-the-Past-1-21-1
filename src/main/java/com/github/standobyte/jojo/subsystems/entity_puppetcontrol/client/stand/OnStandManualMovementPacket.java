package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OnStandManualMovementPacket(int entityId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<OnStandManualMovementPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<OnStandManualMovementPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<OnStandManualMovementPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, OnStandManualMovementPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, OnStandManualMovementPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, OnStandManualMovementPacket::entityId,
				OnStandManualMovementPacket::new);

		@Override
		public void handle(OnStandManualMovementPacket packet, IPayloadContext context) {
			if (ClientProxy.getEntityById(packet.entityId) instanceof StandEntity standEntity) {
				ClientStandController.onMovedStand(standEntity);
			}
		}
		
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
