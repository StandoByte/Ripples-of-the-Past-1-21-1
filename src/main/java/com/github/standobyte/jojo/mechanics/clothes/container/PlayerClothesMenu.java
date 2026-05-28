package com.github.standobyte.jojo.mechanics.clothes.container;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.util.functions.ContainerMenuUtil;
import com.github.standobyte.jojo.util.functions.ContainerMenuUtil.SlotIndices;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlayerClothesMenu extends AbstractContainerMenu {
	public EntityClothesInventory clothesInventory;
	
	public PlayerClothesMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, playerInventory.player);
	}

	public PlayerClothesMenu(int containerId, Inventory playerInventory, Player player) {
		super(ModContainers.PLAYER_CLOTHES.get(), containerId);
		clothesInventory = player.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		initSlots(playerInventory, player);
	}
	
	protected void initSlots(Inventory playerInventory, Player player) {
		for (Slot slot : ContainerMenuUtil.armorSlots(playerInventory, player, 8, 8, slotIndices)) {
			this.addSlot(slot);
		}
		
		for (Slot slot : ContainerMenuUtil.clothesSlots(clothesInventory, player, 77, 8, slotIndices)) {
			this.addSlot(slot);
		}
		
		this.addSlot(ContainerMenuUtil.offhandSlot(playerInventory, player, 95, 62, slotIndices));
		
		for (Slot slot : ContainerMenuUtil.inventorySlots(playerInventory, 8, 84, slotIndices)) {
			this.addSlot(slot);
		}
	}
	
	public SlotIndices slotIndices = new SlotIndices(this);
	
	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int clickedSlot) {
		ItemStack itemResult = ItemStack.EMPTY;
		Slot slot = this.slots.get(clickedSlot);
		if (slot.hasItem()) {
			ItemStack item = slot.getItem();
			itemResult = item.copy();
			@Nullable EquipmentSlot equipSlot = player.getEquipmentSlotForItem(itemResult);
			@Nullable ClothesDataComponent clothes = item.get(ModItemDataComponents.CLOTHES_PIECE);
			@Nullable ClothesSlotType clothesSlot = clothes != null ? clothes.getSlot() : null;

			// take off clothes
			if (clickedSlot >= slotIndices.CLOTHES_START && clickedSlot < slotIndices.CLOTHES_END) {
				if (!this.moveItemStackTo(item, slotIndices.INV_START, slotIndices.INV_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// put on clothes
			else if (clothesSlot != null && isClothesStackable(item, 
					this.slots.get(slotIndices.CLOTHES_START + clothesSlot.ordinal()).getItem())) {
				int clothesSlotIndex = slotIndices.CLOTHES_START + clothesSlot.ordinal();
				if (!this.stackClothes(item, clothesSlotIndex)) {
					return ItemStack.EMPTY;
				}
			} 
			
			// unequip armor
			else if (clickedSlot >= slotIndices.ARMOR_START && clickedSlot < slotIndices.ARMOR_END) {
				if (!this.moveItemStackTo(item, slotIndices.INV_START, slotIndices.INV_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// equip armor
			else if (equipSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && 
					!this.slots.get(slotIndices.ARMOR_END - 1 - equipSlot.getIndex()).hasItem()) {
				int armorSlotIndex = slotIndices.ARMOR_END - 1 - equipSlot.getIndex();
				if (!this.moveItemStackTo(item, armorSlotIndex, armorSlotIndex + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// equip shield
			else if (equipSlot == EquipmentSlot.OFFHAND && !this.slots.get(slotIndices.SHIELD_SLOT).hasItem()) {
				if (!this.moveItemStackTo(item, slotIndices.SHIELD_SLOT, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move from inventory to hotbar
			else if (clickedSlot >= slotIndices.INV_27_START && clickedSlot < slotIndices.INV_27_END) {
				if (!this.moveItemStackTo(item, slotIndices.HOTBAR_START, slotIndices.HOTBAR_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move from hotbar to inventory
			else if (clickedSlot >= slotIndices.HOTBAR_START && clickedSlot < slotIndices.HOTBAR_END) {
				if (!this.moveItemStackTo(item, slotIndices.INV_27_START, slotIndices.INV_27_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move to inventory/hotbar
			else if (!this.moveItemStackTo(item, slotIndices.INV_START, slotIndices.INV_END, false)) {
				return ItemStack.EMPTY;
			}
			

			if (item.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY, itemResult);
			} else {
				slot.setChanged();
			}

			if (item.getCount() == itemResult.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, item);
		}

		return itemResult;
	}

	public boolean moveItemStackTo(ItemStack stack, int slotIndex, boolean reverseDirection) {
		return moveItemStackTo(stack, slotIndex, slotIndex + 1, reverseDirection);
	}
	
	public static boolean isClothesStackable(ItemStack clickedItem, ItemStack itemInClothesSlot) {
		return itemInClothesSlot.isEmpty() || ClothesDataComponent.areDifferentSubpiecesOfTheSamePiece(clickedItem, itemInClothesSlot);
	}
	
	public boolean stackClothes(ItemStack clickedItem, int clothesSlotIndex) {
		Slot clothesSlot = this.slots.get(clothesSlotIndex);
		if (!clothesSlot.hasItem()) {
			return moveItemStackTo(clickedItem, clothesSlotIndex, false);
		}
		
		ItemStack combinedItem = ClothesItem.combineIntoFullPiece(clickedItem, clothesSlot.getItem());
		if (combinedItem != null) {
			clothesSlot.setByPlayer(combinedItem);
			clothesSlot.setChanged();
			clickedItem.setCount(0);
			return true;
		}
		
		return false;
	}


	@Override
	public boolean stillValid(Player player) {
		return true;
	}
	
	
	public static void openOnButtonClick(ServerPlayer player) {
		ContainerMenuUtil.openMenu(player, PlayerClothesMenu::new, Component.translatable("jojo_ripples.menu.player.clothes"));
	}

}
