package com.github.standobyte.jojo.adventure.npc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.mixin.entity_like_player.npc.InventoryAccessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MobAsPlayerInventory extends Inventory {
	protected Mob mob;

	public MobAsPlayerInventory(Player fakePlayer, Mob mob) {
		super(fakePlayer);
		this.mob = mob;
		
		// This makes it so the inventory operations reference the same item collections that are present in the Mob entity
		// (Mob#armorItems and Mob#handItems)
		NonNullList<ItemStack> mobArmor = (NonNullList<ItemStack>) mob.getArmorSlots();
		NonNullList<ItemStack> mobOffhand = new OffHandSlotWrapper(mob);

		InventoryAccessor access = (InventoryAccessor) this;
		List<NonNullList<ItemStack>> compartmentsList = access.getCompartments();
		List<NonNullList<ItemStack>> newCompartments = new ArrayList<>(compartmentsList.size());
		for (var compartment : compartmentsList) {
			if (compartment == this.armor) {
				newCompartments.add(mobArmor);
			}
			else if (compartment == this.offhand) {
				newCompartments.add(mobOffhand);
			}
			else {
				newCompartments.add(compartment);
			}
		}
		access.setCompartments(newCompartments);

		access.setArmor(mobArmor);
		access.setOffhand(mobOffhand);
	}

	@Override
	public void tick() {
		super.tick();
		ItemStack selectedItem = this.getSelected();
		this.mob.setItemInHand(InteractionHand.MAIN_HAND, selectedItem);
	}


	public static class OffHandSlotWrapper extends NonNullList<ItemStack> {

		protected OffHandSlotWrapper(Mob mob) {
			super((List<ItemStack>) mob.getHandSlots(), ItemStack.EMPTY);
		}

		@Nonnull
		@Override
		public ItemStack get(int index) {
			if (index != 0) throw new IllegalArgumentException();
			return super.get(EquipmentSlot.OFFHAND.getIndex());
		}

		@Override
		public ItemStack set(int index, ItemStack value) {
			if (index != 0) throw new IllegalArgumentException();
			Objects.requireNonNull(value);
			return super.set(EquipmentSlot.OFFHAND.getIndex(), value);
		}

		@Override
		public void add(int index, ItemStack value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ItemStack remove(int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public void clear() {
			super.set(EquipmentSlot.OFFHAND.getIndex(), /* this.defaultValue */ ItemStack.EMPTY);
		}

	}

}
