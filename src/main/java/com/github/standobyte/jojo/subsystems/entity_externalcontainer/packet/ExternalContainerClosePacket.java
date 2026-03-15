package com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExternalContainerClosePacket(int containerId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ExternalContainerClosePacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ExternalContainerClosePacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ExternalContainerClosePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ExternalContainerClosePacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ExternalContainerClosePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				ExternalContainerClosePacket::containerId,
				ExternalContainerClosePacket::new);

		@Override
		public void handle(ExternalContainerClosePacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			PlayerExternalContainers.get(player).clientRemoveContainer(payload.containerId);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
