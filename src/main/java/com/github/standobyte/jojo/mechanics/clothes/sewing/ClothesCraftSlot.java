package com.github.standobyte.jojo.mechanics.clothes.sewing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ClothesCraftSlot extends Slot {
	protected final Player player;
	protected int craftCount;
	protected boolean isVisible = true;

	public ClothesCraftSlot(Player pPlayer, Container pSlots, 
			int pSlot, int pXPosition, int pYPosition) {
		super(pSlots, pSlot, pXPosition, pYPosition);
		this.player = pPlayer;
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return false;
	}

	@Override
	public ItemStack remove(int pAmount) {
		if (this.hasItem()) {
			this.craftCount += Math.min(pAmount, this.getItem().getCount());
		}

		return getItem().copy();
	}

	@Override
	protected void onQuickCraft(ItemStack pStack, int pAmount) {
		this.craftCount += pAmount;
		this.checkTakeAchievements(pStack);
	}

	@Override
	protected void checkTakeAchievements(ItemStack pStack) {
		if (this.craftCount > 0) {
			pStack.onCraftedBy(this.player.level(), this.player, this.craftCount);
//			if (player instanceof ServerPlayer serverPlayer) {
//				ModCriteriaTriggers.CRAFT_CLOTHES.get().trigger(serverPlayer);
//			}
		}

		this.craftCount = 0;
	}

	@Override
	public void onTake(Player pPlayer, ItemStack pStack) {
		this.checkTakeAchievements(pStack);
	}


	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	public boolean isActive() {
		return isVisible;
	}
}
