package com.github.standobyte.jojo.util.functions;

import java.util.Map;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.container.ClothesSlot;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

public class ContainerMenuUtil {

	public static Slot[] inventorySlots(Inventory playerInventory, int x, int y) {
		Slot[] slots = new Slot[36];

		// inventory
		for (int row = 0; row < 3; row++) {
			for (int i = 0; i < 9; i++) {
				int slotIndex = i + (row + 1) * 9;
				int slotX = x + i * 18;
				int slotY = y + row * 18;
				slots[slotIndex] = new Slot(playerInventory, slotIndex, slotX, slotY);
			}
		}

		// hotbar
		for (int i = 0; i < 9; i++) {
			int slotIndex = i;
			int slotX = x + i * 18;
			int slotY = y + 58;
			slots[slotIndex] = new Slot(playerInventory, slotIndex, slotX, slotY);
		}

		return slots;
	}

	public static Slot offhandSlot(Inventory playerInventory, LivingEntity player, int x, int y) {
		return new Slot(playerInventory, 40, x, y) {
			@Override
			public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
				player.onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
				super.setByPlayer(newStack, oldStack);
			}

			@Override
			public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
				return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
			}
		};
	}

	public static final Map<EquipmentSlot, ResourceLocation> ARMOR_TEXTURE_EMPTY_SLOTS = Map.of(
			EquipmentSlot.FEET, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
			EquipmentSlot.LEGS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
			EquipmentSlot.CHEST, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
			EquipmentSlot.HEAD, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
	public static final EquipmentSlot[] ARMOR_SLOT_IDS = new EquipmentSlot[] {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET };
	public static Slot[] armorSlots(Inventory playerInventory, LivingEntity player, int x, int y) {
		Slot[] slots = new Slot[4];
		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = ARMOR_SLOT_IDS[i];
			ResourceLocation slotIcon = ARMOR_TEXTURE_EMPTY_SLOTS.get(slot);
			int slotIndex = 39 - i;
			int slotX = x;
			int slotY = y + i * 18;

			slots[i] = new ArmorSlot(playerInventory, player, slot, slotIndex, slotX, slotY, slotIcon);
		}
		return slots;
	}

	public static final Map<ClothesSlotType, ResourceLocation> CLOTHES_TEXTURE_EMPTY_SLOTS = Map.of(
			ClothesSlotType.FEET, JojoMod.resLoc("gui/container/emptyslots/clothes_feet"),
			ClothesSlotType.LEGS, JojoMod.resLoc("gui/container/emptyslots/clothes_legs"),
			ClothesSlotType.CHEST, JojoMod.resLoc("gui/container/emptyslots/clothes_chest"),
			ClothesSlotType.HEAD, JojoMod.resLoc("gui/container/emptyslots/clothes_head"));
	public static Slot[] clothesSlots(EntityClothesInventory clothesInventory, LivingEntity player, int x, int y) {
		Slot[] slots = new Slot[4];
		for (int i = 0; i < 4; i++) {
			ClothesSlotType slot = ClothesSlotType.values()[i];
			ResourceLocation slotIcon = CLOTHES_TEXTURE_EMPTY_SLOTS.get(slot);
			int slotIndex = i;
			int slotX = x;
			int slotY = y + i * 18;

			slots[i] = new ClothesSlot(clothesInventory, player, slot, slotIndex, slotX, slotY, slotIcon);
		}
		return slots;
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
}
