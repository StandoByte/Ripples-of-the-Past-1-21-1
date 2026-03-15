package com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet;

import java.util.List;

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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExternalContainerSyncSetContentPacket(int containerId, int stateId, List<ItemStack> items, ItemStack carriedItem) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ExternalContainerSyncSetContentPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ExternalContainerSyncSetContentPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ExternalContainerSyncSetContentPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ExternalContainerSyncSetContentPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ExternalContainerSyncSetContentPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerSyncSetContentPacket::containerId,
				ByteBufCodecs.VAR_INT,
				ExternalContainerSyncSetContentPacket::stateId,
				ItemStack.OPTIONAL_LIST_STREAM_CODEC,
				ExternalContainerSyncSetContentPacket::items,
				ItemStack.OPTIONAL_STREAM_CODEC,
				ExternalContainerSyncSetContentPacket::carriedItem,
				ExternalContainerSyncSetContentPacket::new);

		@Override
		public void handle(ExternalContainerSyncSetContentPacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			AbstractContainerMenu containerMenu = PlayerExternalContainers.get(player).getContainer(payload.containerId);
			if (containerMenu != null) {
				containerMenu.initializeContents(payload.stateId, payload.items, payload.carriedItem);
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
