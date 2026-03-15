package com.github.standobyte.jojo.subsystems.entity_externalcontainer.client;

import java.util.List;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.ModdedContainerClickType;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ClExtendedContainerClickPacket;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientExtendedInventoryClick {

	public static void slotClicked(Slot slot, int slotId, AbstractContainerMenu container, boolean isExternalContainer, 
			int mouseButton, ModdedContainerClickType clickType) {
		Minecraft mc = Minecraft.getInstance();
		if (!isExternalContainer && container != mc.player.containerMenu) {
			JojoMod.getLogger().warn("Ignoring click in mismatching container. Click in {}, player has {}.", container.containerId, mc.player.containerMenu.containerId);
			return;
		}
		
		List<ItemStack> prevItems = copyItems(container);

		PlayerExternalContainers.click(container, slotId, mouseButton, clickType, mc.player);

		Int2ObjectMap<ItemStack> itemsChanged = filterChanged(prevItems, container);

		PacketDistributor.sendToServer(new ClExtendedContainerClickPacket(container.containerId, isExternalContainer, 
				container.getStateId(), slotId, mouseButton, clickType, mc.player.containerMenu.getCarried().copy(), itemsChanged));
	}
	
	protected static List<ItemStack> copyItems(AbstractContainerMenu container) {
		List<Slot> slots = container.slots;
		List<ItemStack> prevItems = Lists.newArrayListWithCapacity(slots.size());
		for (Slot _slot : slots) {
			prevItems.add(_slot.getItem().copy());
		}
		return prevItems;
	}
	
	protected static Int2ObjectMap<ItemStack> filterChanged(List<ItemStack> prevItems, AbstractContainerMenu container) {
		List<Slot> slots = container.slots;
		Int2ObjectMap<ItemStack> itemsChanged = new Int2ObjectOpenHashMap<>();
		for (int j = 0; j < slots.size(); j++) {
			ItemStack itemPrev = prevItems.get(j);
			ItemStack itemCur = slots.get(j).getItem();
			if (!ItemStack.matches(itemPrev, itemCur)) {
				itemsChanged.put(j, itemCur.copy());
			}
		}
		return itemsChanged;
	}

}
