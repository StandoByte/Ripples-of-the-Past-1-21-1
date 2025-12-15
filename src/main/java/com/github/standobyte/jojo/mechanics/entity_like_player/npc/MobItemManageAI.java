package com.github.standobyte.jojo.mechanics.entity_like_player.npc;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mixin.entity_like_player.npc.InventoryAccessor;
import com.github.standobyte.jojo.util.mc.AttributeUtil;

import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class MobItemManageAI {
	protected static final EquipmentSlot ARMOR_SLOTS[] = new EquipmentSlot[] { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };

	protected Map<EquipmentSlot, ItemSearchResult> _armorSearch = Util.make(new EnumMap<>(EquipmentSlot.class), map -> {
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			map.put(slot, new ItemSearchResult());
		}
	});

	public static class ItemSearchResult {
		int slotIndex;
		InteractionHand heldInHand;
		ItemStack itemStack;
		double armor;
		double armorToughness;
		double dps;
		boolean isShield;
		float hunger;
		float saturation;
		String note;
		
		public ItemSearchResult() { clear(); }

		// in case i add more info here i want there to be a compilation errer
		public void fill(int slotIndex, Inventory inventory, ItemStack item, 
				double armor, double armorToughness, double dps, boolean isShield, 
				float hunger, float saturation, 
				@Nullable String note) {
			this.slotIndex = slotIndex;
			this.heldInHand = 
					inventory != null && slotIndex == inventory.selected ? InteractionHand.MAIN_HAND : 
					slotIndex == Inventory.SLOT_OFFHAND ? InteractionHand.OFF_HAND : 
					null;
			this.itemStack = item;
			this.armor = armor;
			this.armorToughness = armorToughness;
			this.dps = dps;
			this.isShield = isShield;
			this.hunger = hunger;
			this.saturation = saturation;
			this.note = note;
		}

		public void clear() {
			fill(-1, null, ItemStack.EMPTY, 0, 0, 0, false, 0, 0, null);
		}
	}

	public void customServerAiStep(Mob mob, Player playerWrapper) {
		Inventory inventory = playerWrapper.getInventory();
		int slotIndex = 0;

		for (var data : _armorSearch.values()) data.clear();

		double baseArmor = mob.getAttributeBaseValue(Attributes.ARMOR);
		double baseArmorToughness = mob.getAttributeBaseValue(Attributes.ARMOR_TOUGHNESS);
		for (NonNullList<ItemStack> compartment : ((InventoryAccessor) inventory).getCompartments()) {
			for (int i = 0; i < compartment.size(); i++) {
				ItemStack item = compartment.get(i);
				if (!item.isEmpty()) {
					if (compartment != mob.getArmorSlots()) {
						// Look for unequipped armor
						// XXX consider enchantments
						EquipmentSlot equipmentSlot = mob.getEquipmentSlotForItem(item);
						if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && mob.canUseSlot(equipmentSlot)) {
							ItemSearchResult curBestArmor = this._armorSearch.get(equipmentSlot);
							whichArmorIsBetter(curBestArmor, item, 
									equipmentSlot, baseArmor, baseArmorToughness, 
									true, inventory, slotIndex);
						}

						// XXX look for food

						// XXX look for weapon

						// XXX look for shield
					}
				}
				slotIndex++;
			}

			for (EquipmentSlot armorSlot : ARMOR_SLOTS) {
				ItemSearchResult foundArmor = _armorSearch.get(armorSlot);
				if (!foundArmor.itemStack.isEmpty()) {
					ItemStack curWornArmor = mob.getItemBySlot(armorSlot);
					if (curWornArmor.isEmpty() || 
							!EnchantmentHelper.has(curWornArmor, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && whichArmorIsBetter(foundArmor, curWornArmor, 
									armorSlot, baseArmor, baseArmorToughness, 
									false, inventory, -1) < 0) {
						if (foundArmor.heldInHand != null) {
							mob.swing(foundArmor.heldInHand);
						}
						ItemStack prevArmor = curWornArmor.copyAndClear();
						mob.setItemSlot(armorSlot, foundArmor.itemStack.copyAndClear());
						inventory.setItem(foundArmor.slotIndex, prevArmor);
					}
				}
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
	 * @return -1 if the first stack is better, 1 if the second one is. 0 if they are equal.
	 */
	public static int whichArmorIsBetter(ItemSearchResult item1, ItemStack item2, 
			EquipmentSlot equipmentSlot, double baseMobArmor, double baseMobArmorToughness, 
			boolean fillItem1Data, Inventory inventory, int slotIndex) {
		int ret;
		ItemAttributeModifiers modifiers = item2.getAttributeModifiers();
		double item2Armor = AttributeUtil.calculateValue(modifiers, Attributes.ARMOR, equipmentSlot, baseMobArmor);
		double item2ArmorToughness = AttributeUtil.calculateValue(modifiers, Attributes.ARMOR_TOUGHNESS, equipmentSlot, baseMobArmorToughness);
		if (item2Armor >= item1.armor) {
			if (item2Armor > item1.armor || item2ArmorToughness > item1.armorToughness) {
				ret = 1;
			}
			else if (item2ArmorToughness == item1.armorToughness) ret = 0;
			else ret = -1;
		}
		else ret = -1;

		if (fillItem1Data && ret > 0) {
			item1.fill(slotIndex, inventory, item2, item2Armor, item2ArmorToughness, 0, false, 0, 0, null);
		}
		return ret;
	}
}
