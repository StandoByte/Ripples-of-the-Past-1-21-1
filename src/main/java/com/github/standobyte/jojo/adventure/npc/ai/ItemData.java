package com.github.standobyte.jojo.adventure.npc.ai;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.functions.AttributeUtil;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ItemData {
	public final int slotIndex;
	@Nullable public EquipmentSlot equippedInSlot;

	public ItemStack itemStack;
	@Nullable public ItemType itemType;
	
	public boolean isDamageable;
	public int maxDurabilityCountingUnbreaking;

	public double armor;
	public double armorToughness;

	public double dps;

	public float hunger;
	public float saturation;

	@Nullable public String note;

	public ItemData(int slotIndex) {
		this.slotIndex = slotIndex;
		clear();
	}

	public void fill(LivingEntity mob, ItemStack item, double baseArmor, double baseArmorToughness) {
		clear();
		this.itemStack = item;
		
		this.isDamageable = item.isDamageableItem();
		this.maxDurabilityCountingUnbreaking = item.getMaxDamage();

		EquipmentSlot equipmentSlot = mob.getEquipmentSlotForItem(item);
		if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && mob.canUseSlot(equipmentSlot)) {
			this.itemType = ItemType.fromVanillaArmorSlot(equipmentSlot);

			ItemAttributeModifiers modifiers = item.getAttributeModifiers();
			this.armor = AttributeUtil.calculateValue(modifiers, Attributes.ARMOR, equipmentSlot, baseArmor);
			this.armorToughness = AttributeUtil.calculateValue(modifiers, Attributes.ARMOR_TOUGHNESS, equipmentSlot, baseArmorToughness);
		}
	}

	public void clear() {
		this.equippedInSlot = null;
		this.itemStack = ItemStack.EMPTY;
		this.itemType = null;
		this.maxDurabilityCountingUnbreaking = 0;
		this.armor = 0;
		this.armorToughness = 0;
		this.dps = 0;
		this.hunger = 0;
		this.saturation = 0;
		this.note = null;
	}



	public enum ItemType {
		ARMOR_FEET,
		ARMOR_HEAD,
		ARMOR_LEGS,
		ARMOR_CHEST,
		MELEE_WEAPON,
		RANGED_WEAPON,
		FOOD,
		SHIELD;

		public static final ItemType ARMOR_SLOTS[] = new ItemType[] { ARMOR_FEET, ARMOR_HEAD, ARMOR_LEGS, ARMOR_CHEST };
		protected static final EquipmentSlot ARMOR_SLOTS_VANILLA[] = new EquipmentSlot[] { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };

		public static ItemType fromVanillaArmorSlot(EquipmentSlot slot) {
			return switch (slot) {
				case FEET -> ItemType.ARMOR_FEET;
				case HEAD -> ItemType.ARMOR_HEAD;
				case LEGS -> ItemType.ARMOR_LEGS;
				case CHEST -> ItemType.ARMOR_CHEST;
				default -> null;
			};
		}

		public static EquipmentSlot toVanillaArmorSlot(ItemType type) {
			return switch (type) {
				case ARMOR_FEET -> EquipmentSlot.FEET;
				case ARMOR_HEAD -> EquipmentSlot.HEAD;
				case ARMOR_LEGS -> EquipmentSlot.LEGS;
				case ARMOR_CHEST -> EquipmentSlot.CHEST;
				default -> null;
			};
		}

		public static InteractionHand hand(EquipmentSlot slot) {
			return switch (slot) {
				case EquipmentSlot.MAINHAND -> InteractionHand.MAIN_HAND;
				case EquipmentSlot.OFFHAND -> InteractionHand.OFF_HAND;
				default -> null;
			};
		}

	}
}
