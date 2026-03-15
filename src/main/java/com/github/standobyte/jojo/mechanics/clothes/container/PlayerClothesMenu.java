package com.github.standobyte.jojo.mechanics.clothes.container;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlayerClothesMenu extends AbstractContainerMenu {
	public static final Map<EquipmentSlot, ResourceLocation> ARMOR_TEXTURE_EMPTY_SLOTS = Map.of(
			EquipmentSlot.FEET,
			InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
			EquipmentSlot.LEGS,
			InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			EquipmentSlot.CHEST,
			InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
			EquipmentSlot.HEAD,
			InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
	
	public static final EquipmentSlot[] ARMOR_SLOT_IDS = new EquipmentSlot[] {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET };

	
	public static final Map<ClothesSlotType, ResourceLocation> CLOTHES_TEXTURE_EMPTY_SLOTS = Map.of(
			ClothesSlotType.FEET,
			JojoMod.resLoc("gui/container/emptyslots/clothes_feet"),
			ClothesSlotType.LEGS,
			JojoMod.resLoc("gui/container/emptyslots/clothes_legs"),
			ClothesSlotType.CHEST,
			JojoMod.resLoc("gui/container/emptyslots/clothes_chest"),
			ClothesSlotType.HEAD,
			JojoMod.resLoc("gui/container/emptyslots/clothes_head"));

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
		int slotInContainer;
		int x;
		int y;

		// armor
		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = ARMOR_SLOT_IDS[i];
			ResourceLocation slotIcon = ARMOR_TEXTURE_EMPTY_SLOTS.get(slot);
			slotInContainer = 39 - i;
			x = 8;
			y = 8 + i * 18;
			
			this.addSlot(new ArmorSlot(playerInventory, player, slot, slotInContainer, x, y, slotIcon));
		}

		// clothes
		for (int i = 0; i < 4; i++) {
			ClothesSlotType slot = ClothesSlotType.values()[i];
			ResourceLocation slotIcon = CLOTHES_TEXTURE_EMPTY_SLOTS.get(slot);
			slotInContainer = i;
			x = 77;
			y = 8 + i * 18;
			
			this.addSlot(new ClothesSlot(clothesInventory, player, slot, slotInContainer, x, y, slotIcon));
		}

		// offhand
		slotInContainer = 40;
		x = 95;
		y = 62;
		
		this.addSlot(new Slot(playerInventory, slotInContainer, x, y) {
			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				player.onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
				super.setByPlayer(newStack, oldStack);
			}

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});

		// inventory
		for (int row = 0; row < 3; row++) {
			for (int i = 0; i < 9; i++) {
				slotInContainer = i + (row + 1) * 9;
				x = 8 + i * 18;
				y = 84 + row * 18;
				
				this.addSlot(new Slot(playerInventory, slotInContainer, x, y));
			}
		}

		// hotbar
		for (int i = 0; i < 9; i++) {
			slotInContainer = i;
			x = 8 + i * 18;
			y = 142;
			
			this.addSlot(new Slot(playerInventory, i, x, y));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public static final int ARMOR_START = 0;
	public static final int ARMOR_END = 4;
	public static final int CLOTHES_START = 4;
	public static final int CLOTHES_END = 8;
	public static final int SHIELD_SLOT = 8;
	public static final int INV_START = 9;
	public static final int INV_END = 36;
	public static final int HOTBAR_START = 36;
	public static final int HOTBAR_END = 45;
	
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
	
	
	public static void openOnButtonClick(ServerPlayer player) {
		player.doCloseContainer();
		// MenuSupplier, MenuConstructor, MenuProvider, ёбаный ваш рот, какой долбоёб все эти интерфейсы писал, заебёшься в этом ООПшном говне копаться
		player.openMenu(new SimpleMenuProvider(PlayerClothesMenu::new, Component.translatable("jojo_ripples.menu.player.clothes")));
	}

}
