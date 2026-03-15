package com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExternalContainerOpenPacket(int containerId, MenuType<?> menuType, byte[] additionalData) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ExternalContainerOpenPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ExternalContainerOpenPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ExternalContainerOpenPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ExternalContainerOpenPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, ExternalContainerOpenPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.UNSIGNED_SHORT,
				ExternalContainerOpenPacket::containerId,
				ByteBufCodecs.idMapper(BuiltInRegistries.MENU),
				ExternalContainerOpenPacket::menuType,
				NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
				ExternalContainerOpenPacket::additionalData,
				ExternalContainerOpenPacket::new);

		@Override
		public void handle(ExternalContainerOpenPacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			RegistryAccess registryAccess = player.registryAccess();
			final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.additionalData), registryAccess, context.listener().getConnectionType());
			try {
				MenuType<?> menuType = payload.menuType;
				AbstractContainerMenu menu = menuType.create(payload.containerId, player.getInventory(), buf);
				PlayerExternalContainers.get(player).clientAddContainer(menu);
			}
			catch (Throwable t) {
				JojoMod.getLogger().error("Failed to handle advanced open screen from server.", t);
			}
			finally {
				buf.release();
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
