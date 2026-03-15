package com.github.standobyte.jojo.subsystems;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/* this shit was copypasted from net.minecraft.world.entity.player.Inventory
 * and i ain't spending my braincells on rewriting that
 */
public class EntityHandItemsAsInventory<T extends LivingEntity> implements Container {
	public final T entity;
	public List<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
	
	public EntityHandItemsAsInventory(T entity, List<ItemStack> handItemsList) {
		this.entity = entity;
		this.handItems = handItemsList;
	}
	
	// Container stuff
	
	@Override
	public void clearContent() {
		for (InteractionHand hand : InteractionHand.values()) {
			entity.setItemInHand(hand, ItemStack.EMPTY);
		}
		this.setChanged();
	}

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		for (InteractionHand hand : InteractionHand.values()) {
			if (!entity.getItemInHand(hand).isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		InteractionHand hand = InteractionHand.values()[slot];
		return entity.getItemInHand(hand);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		InteractionHand hand = InteractionHand.values()[slot];
		ItemStack item = entity.getItemInHand(hand);
		ItemStack splitStack = !item.isEmpty() && amount > 0 ? item.split(amount) : ItemStack.EMPTY;
		if (!splitStack.isEmpty()) {
			this.setChanged();
		}
		return splitStack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		InteractionHand hand = InteractionHand.values()[slot];
		ItemStack item = entity.getItemInHand(hand);

		if (item.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			entity.setItemInHand(hand, ItemStack.EMPTY);
			return item;
		}
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		InteractionHand hand = InteractionHand.values()[slot];
		entity.setItemInHand(hand, stack);
		this.setChanged();
	}

	@Override
	public void setChanged() {}
	
	@Override
	public boolean stillValid(Player player) {
		return entity != null && entity.isAlive();
	}
	
	// Inventory stuff

	/**
	 * Adds the stack to the first empty slot in the player's inventory. Returns {@code false} if it's not possible to place the entire stack in the inventory.
	 */
	public boolean add(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		} else {
			if (stack.isDamaged()) {
				int slot = this.getFreeSlot();

				if (slot >= 0) {
					setToSlot(slot, stack.copyAndClear());
					this.handItems.get(slot).setPopTime(5);
					return true;
				} else {
					return false;
				}
			} else {
				int i;
				do {
					i = stack.getCount();
					stack.setCount(this.addResource(stack));
				} while (!stack.isEmpty() && stack.getCount() < i);

				return stack.getCount() < i;
			}
		}
	}
	
	protected void setToSlot(int slot, ItemStack item) {
		ItemStack oldItem = this.handItems.set(slot, item);
		EquipmentSlot slotType = switch (slot) {
			case 0 -> EquipmentSlot.MAINHAND;
			case 1 -> EquipmentSlot.OFFHAND;
			default -> throw new IllegalArgumentException();
		};
		entity.onEquipItem(slotType, oldItem, item);
	}

	public int getFreeSlot() {
		for (int i = 0; i < this.handItems.size(); i++) {
			if (this.handItems.get(i).isEmpty()) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * This function stores as many items of an ItemStack as possible in a matching slot and returns the quantity of left over items.
	 */
	private int addResource(ItemStack stack) {
		int i = this.getSlotWithRemainingSpace(stack);
		if (i == -1) {
			i = this.getFreeSlot();
		}

		return i == -1 ? stack.getCount() : this.addResource(i, stack);
	}

	/**
	 * Stores a stack in the player's inventory. It first tries to place it in the selected slot in the player's hotbar, then the offhand slot, then any available/empty slot in the player's inventory.
	 */
	public int getSlotWithRemainingSpace(ItemStack stack) {
		for (int i = 0; i < this.handItems.size(); i++) {
			if (this.hasRemainingSpaceForItem(this.handItems.get(i), stack)) {
				return i;
			}
		}

		return -1;
	}

	private boolean hasRemainingSpaceForItem(ItemStack destination, ItemStack origin) {
		return !destination.isEmpty()
				&& ItemStack.isSameItemSameComponents(destination, origin)
				&& destination.isStackable()
				&& destination.getCount() < this.getMaxStackSize(destination);
	}

	private int addResource(int slot, ItemStack stack) {
		int i = stack.getCount();
		ItemStack itemstack = handItems.get(slot);
		if (itemstack.isEmpty()) {
			itemstack = stack.copyWithCount(0);
			setToSlot(slot, itemstack);
		}

		int j = this.getMaxStackSize(itemstack) - itemstack.getCount();
		int k = Math.min(i, j);
		if (k == 0) {
			return i;
		} else {
			i -= k;
			itemstack.grow(k);
			itemstack.setPopTime(5);
			return i;
		}
	}

}
