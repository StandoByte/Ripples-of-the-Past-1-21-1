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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExternalContainerSyncSetDataPacket(int containerId, int dataId, int value) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ExternalContainerSyncSetDataPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ExternalContainerSyncSetDataPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ExternalContainerSyncSetDataPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ExternalContainerSyncSetDataPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ExternalContainerSyncSetDataPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerSyncSetDataPacket::containerId,
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerSyncSetDataPacket::dataId,
				ByteBufCodecs.VAR_INT,
				ExternalContainerSyncSetDataPacket::value,
				ExternalContainerSyncSetDataPacket::new);

		@Override
		public void handle(ExternalContainerSyncSetDataPacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			AbstractContainerMenu containerMenu = PlayerExternalContainers.get(player).getContainer(payload.containerId);
			if (containerMenu != null) {
				containerMenu.setData(payload.dataId, payload.value);
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
