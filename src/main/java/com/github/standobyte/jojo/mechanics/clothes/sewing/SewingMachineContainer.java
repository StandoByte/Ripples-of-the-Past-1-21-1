package com.github.standobyte.jojo.mechanics.clothes.sewing;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.container.PlayerClothesMenu;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class SewingMachineContainer extends AbstractContainerMenu {
	public final ContainerLevelAccess access;
	public final CraftSlots craftingSlots;

	public SewingMachineContainer(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModContainers.SEWING_MACHINE.get(), containerId);

		Player player = inventory.player;
		EntityClothesInventory clothes = player.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		InvWrapper forgeClothesInventory = new InvWrapper(clothes);

		for (ClothesSlotType clothesSlot : ClothesSlotType.values()) {
			int i = clothesSlot.ordinal();
			addSlot(new SlotItemHandler(forgeClothesInventory, i, 26, 178 + i * 18) {

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack pStack) {
					if (!pStack.isEmpty() && pStack.get(ModItemDataComponents.CLOTHES_PIECE) instanceof ClothesDataComponent clothes) {
						return clothes.getSlot() == clothesSlot;
					}
					return false;
				}

				@Override
				public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
					return Pair.of(InventoryMenu.BLOCK_ATLAS, PlayerClothesMenu.CLOTHES_TEXTURE_EMPTY_SLOTS.get(clothesSlot));
				}
			});
		}

		for(int k = 0; k < 4; ++k) {
			final EquipmentSlot equipmentslottype = PlayerClothesMenu.ARMOR_SLOT_IDS[k];
			addSlot(new Slot(inventory, 39 - k, 95, 178 + k * 18) {

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public boolean mayPlace(ItemStack pStack) {
					return pStack.canEquip(equipmentslottype, player);
				}

				@Override
				public boolean mayPickup(Player pPlayer) {
					ItemStack itemstack = this.getItem();
					return !itemstack.isEmpty() && !pPlayer.isCreative() && EnchantmentHelper.has(itemstack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ? false : super.mayPickup(pPlayer);
				}

				@Override
				public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
					return Pair.of(InventoryMenu.BLOCK_ATLAS, PlayerClothesMenu.ARMOR_TEXTURE_EMPTY_SLOTS.get(equipmentslottype));
				}
			});
		}

		this.addSlot(new Slot(inventory, 40, 113, 232) {

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		});

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				addSlot(new Slot(inventory, 
						col + row * 9 + 9, 
						8 + col * 18
						+ 173, 
						84 + row * 18
						+ 92
						));
			}
		}
		for (int hotbarCol = 0; hotbarCol < 9; ++hotbarCol) {
			addSlot(new Slot(inventory, 
					hotbarCol, 
					8 + hotbarCol * 18
					+ 173, 
					142
					+ 92
					));
		}

		craftingSlots = new CraftSlots(this, player, 26, 143);

//		addSlot(new Slot(teInventory, 0, 307, 113)); // string item storage
//		int rows = 4;
//		int columns = 4;
//		for (int i = 0; i < rows; i++) { // wool item storage
//			for (int j = 0; j < columns; j++) {
//				addSlot(new Slot(teInventory, 1 + i * columns + j, j * 18 + 271, i * 18 + 41));
//			}
//		}


		this.access = access;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, ModBlocks.SEWING_MACHINE.get());
	}

	/*
	 *  0 -  3: clothes
	 *  4 -  7: armor
	 *       8: off-hand slot
	 *  9 - 35: inventory
	 * 36 - 44: hotbar
	 * 45 - 48: clothes craft slot
	 *      49: string storage
	 * 50 - 65: wool storage
	 */
	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(pIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack clickedItem = slot.getItem();
			itemstack = clickedItem.copy();
			EquipmentSlot armorSlot = getEquipmentSlotForItem(itemstack);
			if (pIndex >= 0 && pIndex < 4) { // clothes
				if (!this.moveItemStackTo(clickedItem, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex >= 4 && pIndex < 8) { // armor
				if (!this.moveItemStackTo(clickedItem, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (armorSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && !this.slots.get(7 - armorSlot.getIndex()).hasItem()) { // equip armor
				int i = 7 - armorSlot.getIndex();
				if (!this.moveItemStackTo(clickedItem, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex < 45 && itemstack.get(ModItemDataComponents.CLOTHES_PIECE) instanceof ClothesDataComponent clothes) {
				ClothesSlotType clothesType = clothes.getSlot();
				int i = clothesType.ordinal();
				if (!this.moveItemStackTo(clickedItem, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (armorSlot == EquipmentSlot.OFFHAND && !this.slots.get(8).hasItem()) { // equip to off-hand slot
				if (!this.moveItemStackTo(clickedItem, 8, 9, false)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex >= 9 && pIndex < 36) { // inventory
				if (!this.moveItemStackTo(clickedItem, 36, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex >= 36 && pIndex < 45) { // hotbar
				if (!this.moveItemStackTo(clickedItem, 9, 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (pIndex >= 45 && pIndex < 49) { // clothes craft slots
				ItemStack clothesCraft = clickedItem.copy();
				if (!this.moveItemStackTo(clothesCraft, 0, 4, false) && !this.moveItemStackTo(clothesCraft, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(clothesCraft, itemstack);
			} else if (!this.moveItemStackTo(clickedItem, 9, 45, false)) { // move from block inventory/off-hand slot to player inventory
				return ItemStack.EMPTY;
			}

			if (clickedItem.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (clickedItem.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(pPlayer, clickedItem);
			if (pIndex == 0) {
				pPlayer.drop(clickedItem, false);
			}
		}

		return itemstack;
	}

	public static EquipmentSlot getEquipmentSlotForItem(ItemStack stack) {
		final EquipmentSlot slot = stack.getEquipmentSlot();
		if (slot != null) {
			return slot;
		}
		Equipable equipable = Equipable.get(stack);
		if (equipable != null) {
			EquipmentSlot equipmentSlot = equipable.getEquipmentSlot();
			return equipmentSlot;
		}

		return EquipmentSlot.MAINHAND;
	}



	public static class CraftSlots implements Container {
		protected static final int SIZE = 4;
		protected final List<ItemStack> allItemsList;
		public final List<Slot> slots;

		protected CraftSlots(SewingMachineContainer container, Player player, int x, int y) {
			List<Slot> craftSlots = new ArrayList<>();
			for (int i = 0; i < SIZE; i++) {
				craftSlots.add(container.addSlot(new ClothesCraftSlot(player, this, i, 
						x + i * 18, y)));
			}
			this.slots = craftSlots;

			this.allItemsList = NonNullList.withSize(4, ItemStack.EMPTY);
		}

		@Override
		public int getContainerSize() {
			return allItemsList.size();
		}

		@Override
		public ItemStack getItem(int pIndex) {
			return pIndex >= getContainerSize() ? ItemStack.EMPTY : allItemsList.get(pIndex);
		}

		@Override
		public void setItem(int pIndex, ItemStack pStack) {
			allItemsList.set(pIndex, pStack);
		}
		
		public void fillFrom(Holder<ClothesSet> clothesSet) {
			int slotIndex = 0;
			for (ClothesSlotType slot : ClothesSlotType.values()) {
				ItemStack item = ModItems.CLOTHES_BASE_ITEM.get().makeClothesPieceStack(clothesSet, slot);
				this.slots.get(slotIndex++).set(item);
			}
		}
		
		public static ItemStack getItem(List<Slot> slots, ClothesSlotType slot) {
			return slots.get(slot.ordinal()).getItem();
		}



		@Override
		public void clearContent() {}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@Override
		public ItemStack removeItem(int pIndex, int pCount) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int pIndex) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setChanged() {}

		@Override
		public boolean stillValid(Player pPlayer) {
			return true;
		}

	}
}
