package com.github.standobyte.jojo.mechanics.explorer_map_creation.packets;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyExplorerMapContainerMenu;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyTableMode;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClCartographyContainerModePacket implements CustomPacketPayload {
	private final CartographyTableMode mode;
	private final BlockPos blockPos;
	
	public ClCartographyContainerModePacket(CartographyTableMode mode, BlockPos blockPos) {
		this.mode = mode;
		this.blockPos = blockPos;
	}
	
	
	
	private static CustomPacketPayload.Type<ClCartographyContainerModePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClCartographyContainerModePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClCartographyContainerModePacket> type() {
			return type;
		}

		@Override
		public void encode(ClCartographyContainerModePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.mode);
			buf.writeBlockPos(packet.blockPos);
		}

		@Override
		public ClCartographyContainerModePacket decode(RegistryFriendlyByteBuf buf) {
			CartographyTableMode mode = buf.readEnum(CartographyTableMode.class);
			BlockPos blockPos = buf.readBlockPos();
			return new ClCartographyContainerModePacket(mode, blockPos);
		}

		@Override
		public void handle(ClCartographyContainerModePacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			Level level = player.level();
			
			final MenuConstructor menuClass;
			final Component menuName;
			switch (payload.mode) {
				case VANILLA -> {
					menuClass = (containerId, inventory, _player) -> new CartographyTableMenu(
							containerId, inventory, ContainerLevelAccess.create(level, payload.blockPos));
					menuName = CartographyExplorerMapContainerMenu.VANILLA_CONTAINER_TITLE;
				}
				case EXPLORER -> {
					menuClass = (containerId, inventory, _player) -> new CartographyExplorerMapContainerMenu(
							containerId, player, inventory, ContainerLevelAccess.create(level, payload.blockPos));
					menuName = CartographyExplorerMapContainerMenu.EXPLORER_CONTAINER_TITLE;
				}
				default -> throw new AssertionError();
			}
			
			MenuProvider menuProvider = new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return menuName;
				}

				@Override
				public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
					return menuClass.createMenu(containerId, playerInventory, player);
				}

				// and allat just to make the mouse cursor not move to the middle of the screen
				@Override
				public boolean shouldTriggerClientSideContainerClosingOnOpen() {
					return false;
				}
			};
			player.openMenu(menuProvider);
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
