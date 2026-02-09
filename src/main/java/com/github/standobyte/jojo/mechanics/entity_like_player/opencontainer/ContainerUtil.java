package com.github.standobyte.jojo.mechanics.entity_like_player.opencontainer;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class ContainerUtil {

	public static Container possiblyGetContainerInventory(AbstractContainerMenu containerMenu) {
		if (containerMenu.slots.isEmpty()) return null;
		
		Slot firstSlot = containerMenu.slots.get(0);
		return firstSlot.container;
	}
	
	public static boolean isSameContainer(Container blockEntityOrSmth, Container containerFromMethodAbove) {
		return containerFromMethodAbove == blockEntityOrSmth 
				|| containerFromMethodAbove instanceof CompoundContainer doubleChest && doubleChest.contains(blockEntityOrSmth);
	}
}
