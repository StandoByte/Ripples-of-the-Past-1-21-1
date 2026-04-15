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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
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
		ARMOR_START = slots.size();
		for (Slot slot : ContainerMenuUtil.armorSlots(playerInventory, player, 8, 8)) {
			this.addSlot(slot);
		}
		ARMOR_END = slots.size();
		
		CLOTHES_START = slots.size();
		for (Slot slot : ContainerMenuUtil.clothesSlots(clothesInventory, player, 77, 8)) {
			this.addSlot(slot);
		}
		CLOTHES_END = slots.size();
		
		SHIELD_SLOT = slots.size();
		this.addSlot(ContainerMenuUtil.offhandSlot(playerInventory, player, 95, 62));
		
		INV_START = slots.size();
		for (Slot slot : ContainerMenuUtil.inventorySlots(playerInventory, 8, 84)) {
			this.addSlot(slot);
		}
		INV_END = slots.size() - 9;
		HOTBAR_START = INV_END;
		HOTBAR_END = HOTBAR_START + 9;
	}
	
	public int ARMOR_START = 0;
	public int ARMOR_END = 4;
	public int CLOTHES_START = 4;
	public int CLOTHES_END = 8;
	public int SHIELD_SLOT = 8;
	public int INV_START = 9;
	public int INV_END = 36;
	public int HOTBAR_START = 36;
	public int HOTBAR_END = 45;
	
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
			if (clickedSlot >= CLOTHES_START && clickedSlot < CLOTHES_END) {
				if (!this.moveItemStackTo(item, INV_START, HOTBAR_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// put on clothes
			else if (clothesSlot != null && isClothesStackable(item, this.slots.get(CLOTHES_START + clothesSlot.ordinal()).getItem())) {
				int clothesSlotIndex = CLOTHES_START + clothesSlot.ordinal();
				if (!this.stackClothes(item, clothesSlotIndex)) {
					return ItemStack.EMPTY;
				}
			} 
			
			// unequip armor
			else if (clickedSlot >= ARMOR_START && clickedSlot < ARMOR_END) {
				if (!this.moveItemStackTo(item, INV_START, HOTBAR_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// equip armor
			else if (equipSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(ARMOR_END - 1 - equipSlot.getIndex()).hasItem()) {
				int armorSlotIndex = ARMOR_END - 1 - equipSlot.getIndex();
				if (!this.moveItemStackTo(item, armorSlotIndex, armorSlotIndex + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// equip shield
			else if (equipSlot == EquipmentSlot.OFFHAND && !this.slots.get(SHIELD_SLOT).hasItem()) {
				if (!this.moveItemStackTo(item, SHIELD_SLOT, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move from inventory to hotbar
			else if (clickedSlot >= INV_START && clickedSlot < INV_END) {
				if (!this.moveItemStackTo(item, HOTBAR_START, HOTBAR_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move from hotbar to inventory
			else if (clickedSlot >= HOTBAR_START && clickedSlot < HOTBAR_END) {
				if (!this.moveItemStackTo(item, INV_START, INV_END, false)) {
					return ItemStack.EMPTY;
				}
			}
			
			// move to inventory/hotbar
			else if (!this.moveItemStackTo(item, INV_START, HOTBAR_END, false)) {
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
		player.doCloseContainer();
		// MenuSupplier, MenuConstructor, MenuProvider, ёбаный ваш рот, какой долбоёб все эти интерфейсы писал, заебёшься в этом ООПшном говне копаться
		player.openMenu(new SimpleMenuProvider(PlayerClothesMenu::new, Component.translatable("jojo_ripples.menu.player.clothes")));
	}

}
