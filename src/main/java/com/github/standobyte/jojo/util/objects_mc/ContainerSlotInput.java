package com.github.standobyte.jojo.util.objects_mc;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods._FriendlyByteBuf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public record ContainerSlotInput(
		int containerId, 	// The id of the window which was clicked. 0 for player inventory.
		int slotNum 		// Id of the clicked slot
		) {
	
	@Nullable
	public static ContainerSlotInput cl_HoveredSlot() {
		if (Minecraft.getInstance().screen instanceof AbstractContainerScreen invScreen) {
			Slot slot = invScreen.getSlotUnderMouse();
			if (slot != null) {
				int index = switch (invScreen) {
					case CreativeModeInventoryScreen killMe -> {
						int anotherIndexWtfMojang = slot.getSlotIndex();
						int matchingIndex = anotherIndexWtfMojang;
						CreativeModeTab.Type tabType = CreativeModeInventoryScreen.selectedTab.getType();
						switch (tabType) {
							case INVENTORY -> {
								// hotbar
								if (anotherIndexWtfMojang >= 0 && anotherIndexWtfMojang <= 8) {
									matchingIndex += 36;
								}
								// armor slots
								if (anotherIndexWtfMojang >= 36 && anotherIndexWtfMojang <= 39) {
									matchingIndex = 44 - anotherIndexWtfMojang;
								}
								// offhand
								else if (anotherIndexWtfMojang == 40) {
									matchingIndex = 45;
								}
								// otherwise that's one of the 27 regular slots, for which the indices match
							}
							// when you have another creative tab open
							default -> {
								// hotbar
								if (anotherIndexWtfMojang >= 0 && anotherIndexWtfMojang <= 8 && anotherIndexWtfMojang != slot.index) {
									matchingIndex += 36;
								}
								// otherwise it's a creative tab slot, which are illegal here
								else {
									matchingIndex = -1;
								}
							}
						}
						yield matchingIndex;
					}

					default -> slot.index;
				};
				return index >= 0 ? new ContainerSlotInput(invScreen.getMenu().containerId, index) : null;
			}
		}
		return null;
	}
	
	@Nullable
	public Slot getSlot(Player player) {
		if (player.containerMenu.containerId == this.containerId()) {
			if (!player.containerMenu.stillValid(player)) {
				JojoMod.getLogger().debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
			} else {
				if (!player.containerMenu.isValidSlotIndex(this.slotNum())) {
					JojoMod.getLogger().debug("Player {} interacted with invalid slot index: {}, available slots: {}", 
							player.getName(), this.slotNum(), player.containerMenu.slots.size());
				}
				else {
					Slot slot = player.containerMenu.getSlot(this.slotNum());

//					if (input.stateId != player.containerMenu.getStateId()) {
//						player.containerMenu.broadcastFullState();
//					}

					return slot;
				}
			}
		}

		return null;
	}
	
	public ItemStack getItem(Player player) {
		Slot slot = getSlot(player);
		return slot != null ? slot.getItem() : ItemStack.EMPTY;
	}
	
	@Deprecated
	public static ItemStack getItem(ContainerSlotInput data, Player player) {
		return data.getItem(player);
	}

	public static final StreamCodec<? super FriendlyByteBuf, ContainerSlotInput> STREAM_CODEC = new StreamCodec<>() {
		
		@Override public void encode(FriendlyByteBuf buffer, ContainerSlotInput value) {
			_FriendlyByteBuf.writeContainerId(buffer, value.containerId());
			buffer.writeShort(value.slotNum());
		}
		
		@Override public ContainerSlotInput decode(FriendlyByteBuf buffer) {
			int containerId = _FriendlyByteBuf.readContainerId(buffer);
			int slotNum = buffer.readShort();
			return new ContainerSlotInput(containerId, slotNum);
		}
	};

}
