package com.github.standobyte.jojo.adventure;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.standdisc.StandDiscItem;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StandAsDiscSlot extends Slot {
	public LivingEntity entity;
	public StandPower standData;

	public StandAsDiscSlot(LivingEntity entity, int x, int y) {
		super(new SimpleContainer(1), 0, x, y);
		this.entity = entity;
		this.standData = StandPower.get(entity);
		updateStandDiscItemTick();
	}

	
	// TODO call this ticking method from somewhere
	public void updateStandDiscItemTick() {
		StandInstance stand = standData != null ? standData.getStandInstance().orElse(null) : null;
		if (stand == null) {
			set(ItemStack.EMPTY);
		}
		else {
			boolean matchesStand = false;
			ItemStack curDiscItem = getItem();
			if (!curDiscItem.isEmpty() && curDiscItem.is(ModItems.STAND_DISC)) {
				StandInstance discStand = StandDiscItem.getStandInstance(curDiscItem);
				matchesStand = discStand.equals(stand);
			}
			if (!matchesStand) {
				set(StandDiscItem.withStand(stand));
			}
		}
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.is(ModItems.STAND_DISC);
	}

	@Override
	public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
		super.setByPlayer(newStack, oldStack);
		if (standData == null) {
			standData = PowerClass.STAND.attachGet(entity);
		}
		if (standData != null && !entity.level().isClientSide()) {
			if (!newStack.isEmpty() && newStack.has(ModItemDataComponents.DISC_STAND)) {
				StandInstance discStand = StandDiscItem.getStandInstance(newStack);
				standData.setStandInstance(Optional.ofNullable(discStand));
			}
			else {
				standData.setStandInstance(Optional.empty());
			}
		}
	}

	//@Override
	//public boolean mayPickup(Player player) {
	//	return true;
	//}

	@Override
	public boolean isFake() {
		return true;
	}

}
