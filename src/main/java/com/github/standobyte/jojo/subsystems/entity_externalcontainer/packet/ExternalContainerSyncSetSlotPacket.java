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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExternalContainerSyncSetSlotPacket(int containerId, int stateId, int slot, ItemStack itemStack) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ExternalContainerSyncSetSlotPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ExternalContainerSyncSetSlotPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ExternalContainerSyncSetSlotPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ExternalContainerSyncSetSlotPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ExternalContainerSyncSetSlotPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerSyncSetSlotPacket::containerId,
				ByteBufCodecs.VAR_INT,
				ExternalContainerSyncSetSlotPacket::stateId,
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerSyncSetSlotPacket::slot,
				ItemStack.OPTIONAL_STREAM_CODEC,
				ExternalContainerSyncSetSlotPacket::itemStack,
				ExternalContainerSyncSetSlotPacket::new);

		@Override
		public void handle(ExternalContainerSyncSetSlotPacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			AbstractContainerMenu containerMenu = PlayerExternalContainers.get(player).getContainer(payload.containerId);
			if (containerMenu != null) {
				containerMenu.setItem(payload.slot, payload.stateId, payload.itemStack);
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
