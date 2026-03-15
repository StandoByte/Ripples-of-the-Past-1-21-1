package com.github.standobyte.jojo.subsystems.entity_externalcontainer;

import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.ClientStandHeldItemsUI;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.StandHandsContainerMenu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public enum ModdedContainerClickType {
	PICKUP(ClickType.PICKUP),
	QUICK_MOVE(ClickType.QUICK_MOVE),
	SWAP(ClickType.SWAP),
	CLONE(ClickType.CLONE),
	THROW(ClickType.THROW),
	QUICK_CRAFT(ClickType.QUICK_CRAFT),
	PICKUP_ALL(ClickType.PICKUP_ALL), 
	
	STAND_QUICK_MOVE();
	
	public final ClickType vanillaType;
	
	private ModdedContainerClickType() {
		this(null);
	}
	
	private ModdedContainerClickType(ClickType vanillaType) {
		this.vanillaType = vanillaType;
	}
	

	public static ModdedContainerClickType fromVanilla(ClickType clickType) {
		return switch (clickType) {
			case PICKUP -> PICKUP;
			case CLONE -> CLONE;
			case PICKUP_ALL -> PICKUP_ALL;
			case QUICK_CRAFT -> QUICK_CRAFT;
			case QUICK_MOVE -> QUICK_MOVE;
			case SWAP -> SWAP;
			case THROW -> THROW;
			default -> throw new AssertionError();
		};
	}

	
	public static ModdedContainerClickType getClientModdedClick(Screen containerScreen, AbstractContainerMenu mainContainer, 
			Slot slot, int slotId, int mouseButton, ClickType clickType) {
		ModdedContainerClickType standUIClick = ClientStandHeldItemsUI.getStandQolClickType(clickType, 
				containerScreen, mainContainer, slot, slotId, mouseButton);
		if (standUIClick != null) {
			return standUIClick;
		}
		return null;
	}

	public static void clicked(AbstractContainerMenu clickedContainer, int slotId, int mouseButton, 
			ModdedContainerClickType moddedClickType, Player player) {
		Slot clickedSlot = slotId > 0 && slotId < clickedContainer.slots.size() ? clickedContainer.getSlot(slotId) : null;
		switch (moddedClickType) {
			case STAND_QUICK_MOVE -> {
				StandHandsContainerMenu standHandsContainer = PlayerExternalContainers.get(player)
						.getContainerOfType(StandHandsContainerMenu.class);
				if (standHandsContainer != null && clickedSlot != null) {
					standHandsContainer.handleCtrlClickAKAStandQuickMove(clickedContainer, slotId, mouseButton, player);
				}
			}
			default -> {}
		}
	}
}
