package com.github.standobyte.jojo.util.functions;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.container.ClothesSlot;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

public class ContainerMenuUtil {
	
	public static void openMenu(ServerPlayer player, MenuConstructor menuConstructor, Component title) {
		player.doCloseContainer();
		// MenuSupplier, MenuConstructor, MenuProvider, ёбаный ваш рот, какой долбоёб все эти интерфейсы писал, заебёшься в этом ООПшном говне копаться
		player.openMenu(new SimpleMenuProvider(menuConstructor, title));
	}
	

	public static Slot[] inventorySlots(Inventory playerInventory, int x, int y, @Nullable SlotIndices memorizeIndices) {
		Slot[] slots = new Slot[36];
		int i = 0;

		// inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = col + (row + 1) * 9;
				int slotX = x + col * 18;
				int slotY = y + row * 18;
				slots[i++] = new Slot(playerInventory, slotIndex, slotX, slotY);
			}
		}

		// hotbar
		for (int col = 0; col < 9; col++) {
			int slotIndex = col;
			int slotX = x + col * 18;
			int slotY = y + 58;
			slots[i++] = new Slot(playerInventory, slotIndex, slotX, slotY);
		}
		
		if (memorizeIndices != null) {
			memorizeIndices.INV_START = memorizeIndices.latestIndex();
			memorizeIndices.INV_END = memorizeIndices.INV_START + slots.length;
			
			memorizeIndices.INV_27_START = memorizeIndices.INV_START;
			memorizeIndices.HOTBAR_START = memorizeIndices.INV_START + 27;
			
			memorizeIndices.INV_27_END = memorizeIndices.INV_27_START + 27;
			memorizeIndices.HOTBAR_END = memorizeIndices.HOTBAR_START + 9;
		}

		return slots;
	}

	public static Slot offhandSlot(Inventory playerInventory, LivingEntity player, int x, int y, @Nullable SlotIndices memorizeIndices) {
		Slot slot = new Slot(playerInventory, 40, x, y) {
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
		
		if (memorizeIndices != null) {
			memorizeIndices.SHIELD_SLOT = memorizeIndices.latestIndex();
		}
		
		return slot;
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
	public static Slot[] armorSlots(Inventory playerInventory, LivingEntity player, int x, int y, @Nullable SlotIndices memorizeIndices) {
		Slot[] slots = new Slot[4];
		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = ARMOR_SLOT_IDS[i];
			ResourceLocation slotIcon = ARMOR_TEXTURE_EMPTY_SLOTS.get(slot);
			int slotIndex = 39 - i;
			int slotX = x;
			int slotY = y + i * 18;

			slots[i] = new ArmorSlot(playerInventory, player, slot, slotIndex, slotX, slotY, slotIcon);
		}
		
		if (memorizeIndices != null) {
			memorizeIndices.ARMOR_START = memorizeIndices.latestIndex();
			memorizeIndices.ARMOR_END = memorizeIndices.ARMOR_START + slots.length;
		}
		
		return slots;
	}

	public static final Map<ClothesSlotType, ResourceLocation> CLOTHES_TEXTURE_EMPTY_SLOTS = Map.of(
			ClothesSlotType.FEET, JojoMod.resLoc("gui/container/emptyslots/clothes_feet"),
			ClothesSlotType.LEGS, JojoMod.resLoc("gui/container/emptyslots/clothes_legs"),
			ClothesSlotType.CHEST, JojoMod.resLoc("gui/container/emptyslots/clothes_chest"),
			ClothesSlotType.HEAD, JojoMod.resLoc("gui/container/emptyslots/clothes_head"));
	public static Slot[] clothesSlots(EntityClothesInventory clothesInventory, LivingEntity player, int x, int y, @Nullable SlotIndices memorizeIndices) {
		Slot[] slots = new Slot[4];
		for (int i = 0; i < 4; i++) {
			ClothesSlotType slot = ClothesSlotType.values()[i];
			ResourceLocation slotIcon = CLOTHES_TEXTURE_EMPTY_SLOTS.get(slot);
			int slotIndex = i;
			int slotX = x;
			int slotY = y + i * 18;

			slots[i] = new ClothesSlot(clothesInventory, player, slot, slotIndex, slotX, slotY, slotIcon);
		}
		
		if (memorizeIndices != null) {
			memorizeIndices.CLOTHES_START = memorizeIndices.latestIndex();
			memorizeIndices.CLOTHES_END = memorizeIndices.CLOTHES_START + slots.length;
		}
		
		return slots;
	}
	
	/** Helper class to track indices for quickMoveStack implementation */
	public static class SlotIndices {
		public List<Slot> containerSlots;
		
		public SlotIndices(AbstractContainerMenu container) {
			this(container.slots);
		}
		
		public SlotIndices(List<Slot> containerSlots) {
			this.containerSlots = containerSlots;
		}
		
		public int latestIndex() {
			return containerSlots.size();
		}
		
		public Integer ARMOR_START;
		public Integer ARMOR_END;
		public Integer CLOTHES_START;
		public Integer CLOTHES_END;
		public Integer SHIELD_SLOT;

		public Integer INV_START;
		public Integer INV_END;
		public Integer HOTBAR_START;
		public Integer HOTBAR_END;
		public Integer INV_27_START;
		public Integer INV_27_END;
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
