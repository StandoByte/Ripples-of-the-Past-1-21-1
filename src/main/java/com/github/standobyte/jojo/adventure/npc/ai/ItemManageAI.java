package com.github.standobyte.jojo.adventure.npc.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.adventure.npc.ai.ItemData.ItemType;
import com.github.standobyte.jojo.mixin.entity_like_player.npc.InventoryAccessor;

import net.minecraft.core.NonNullList;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class ItemManageAI {
	protected List<ItemData> _inventoryItemData = new ArrayList<>(41);
	protected Map<ItemType, List<ItemData>> _byItemType = new EnumMap<>(ItemType.class);
	
	protected ItemData getItemData(int invSlotIndex) {
		if (invSlotIndex >= _inventoryItemData.size()) {
			int toFill = invSlotIndex - _inventoryItemData.size() + 1;
			for (int i = 0; i < toFill; i++) {
				ItemData data = new ItemData(_inventoryItemData.size());
				_inventoryItemData.add(data);
			}
		}
		return _inventoryItemData.get(invSlotIndex);
	}

	public void cacheItemData(LivingEntity mob, Inventory inventory) {
		for (List<ItemData> group : _byItemType.values()) {
			group.clear();
		}
		int slotIndex = 0;
		
		double mobBaseArmor = mob.getAttributeBaseValue(Attributes.ARMOR);
		double mobBaseToughness = mob.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS);
		
		for (NonNullList<ItemStack> compartment : ((InventoryAccessor) inventory).getCompartments()) {
			for (int i = 0; i < compartment.size(); i++) {
				ItemStack item = compartment.get(i);
				ItemData data = getItemData(slotIndex);
				data.fill(mob, item, mobBaseArmor, mobBaseToughness);
				
				if (inventory != null && slotIndex == inventory.selected) {
					data.equippedInSlot = EquipmentSlot.MAINHAND;
				}
				else if (slotIndex == Inventory.SLOT_OFFHAND) {
					data.equippedInSlot = EquipmentSlot.OFFHAND;
				}
				else if (compartment == mob.getArmorSlots()) {
					for (EquipmentSlot slot : ItemType.ARMOR_SLOTS_VANILLA) {
						if (slot.getIndex() == i) {
							data.equippedInSlot = slot;
						}
					}
				}
				
				if (data.itemType != null) {
					List<ItemData> group = _byItemType.computeIfAbsent(data.itemType, __ -> new ArrayList<>());
					group.add(data);
				}
				
				slotIndex++;
			}
		}
	}

	protected Map<EquipmentSlot, ItemData> _curBestArmor = new EnumMap<>(EquipmentSlot.class);
	public void customServerAiStep(Mob mob, Player playerWrapper) {
		Inventory inventory = playerWrapper.getInventory();
		cacheItemData(mob, inventory);

		for (ItemType slot : ItemType.ARMOR_SLOTS) {
			if (slot != ItemType.ARMOR_HEAD) continue;
			List<ItemData> items = _byItemType.get(slot);
			if (items != null && !items.isEmpty()) {
				ItemData bestArmor = items.size() > 1 ?
						items.stream().max((item1, item2) -> ItemManageAI.whichArmorIsBetter(item1, item2)).get() : 
						items.get(0);
				EquipmentSlot armorSlot = ItemType.toVanillaArmorSlot(slot);
				ItemStack curWornArmor = mob.getItemBySlot(armorSlot);
				if (curWornArmor != bestArmor.itemStack) {
					if (bestArmor.equippedInSlot != null) {
						InteractionHand heldInHand = ItemType.hand(bestArmor.equippedInSlot);
						if (heldInHand != null) {
							mob.swing(heldInHand);
						}
					}
					ItemStack prevArmor = curWornArmor.copyAndClear();
					mob.setItemSlot(armorSlot, bestArmor.itemStack.copyAndClear());
					inventory.setItem(bestArmor.slotIndex, prevArmor);
				}
				
				// XXX look for food
				// XXX look for weapon
				// XXX look for shield
			}
		}
	}

	public static InteractionResultHolder<ItemStack> swapWithEquipmentSlot(Item item, Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		EquipmentSlot equipmentslot = player.getEquipmentSlotForItem(itemstack);
		if (!player.canUseSlot(equipmentslot)) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			ItemStack itemstack1 = player.getItemBySlot(equipmentslot);
			if ((!EnchantmentHelper.has(itemstack1, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || player.isCreative())
					&& !ItemStack.matches(itemstack, itemstack1)) {
				if (!level.isClientSide()) {
					player.awardStat(Stats.ITEM_USED.get(item));
				}

				ItemStack itemstack2 = itemstack1.isEmpty() ? itemstack : itemstack1.copyAndClear();
				ItemStack itemstack3 = player.isCreative() ? itemstack.copy() : itemstack.copyAndClear();
				player.setItemSlot(equipmentslot, itemstack3);
				return InteractionResultHolder.sidedSuccess(itemstack2, level.isClientSide());
			} else {
				return InteractionResultHolder.fail(itemstack);
			}
		}
	}

	/**
	 * @return 1 if the first stack is better, -1 if the second one is. 0 if they are equal.
	 */
	public static int whichArmorIsBetter(ItemData item1, ItemData item2) {
		int ret;
		if (item1.armor >= item2.armor) {
			if (item1.armor > item2.armor || item1.armorToughness > item2.armorToughness) {
				ret = 1;
			}
			else if (item1.armorToughness == item2.armorToughness) ret = 0;
			else ret = -1;
		}
		else ret = -1;
		
		if (ret == 0) {
			if (item1.isDamageable != item2.isDamageable) ret = item1.isDamageable ? -1 : 1;
			if (ret == 0 && item1.isDamageable && item2.isDamageable) ret = Integer.compare(item1.maxDurabilityCountingUnbreaking, item2.maxDurabilityCountingUnbreaking);
			// XXX consider durability enchantment
		}
		
		
		if (ret == 0) {
			if (item1.equippedInSlot != null && item1.equippedInSlot.isArmor()) ret = -1;
			else if (item2.equippedInSlot != null && item2.equippedInSlot.isArmor()) ret = 1;
		}
		return ret;
	}
}
