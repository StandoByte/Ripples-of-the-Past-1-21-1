package com.github.standobyte.jojo.mechanics.clothes.sewing;

import java.util.function.Supplier;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class HideableSlot extends Slot {
	protected Supplier<Boolean> activeCondition = () -> true;

	public HideableSlot(Inventory container, int slot, int x, int y) {
		super(container, slot, x, y);
	}

	public HideableSlot setActiveWhen(Supplier<Boolean> condition) {
		this.activeCondition = condition;
		return this;
	}

	@Override
	public boolean isActive() {
		return activeCondition.get();
	}
}
