package com.github.standobyte.jojo.util.mc;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public class ItemUtil {

	public static <T> T getFromEmptyItem(ItemStack item, DataComponentType<? extends T> component) {
		return item.components.get(component);
	}

	public static ItemStack copyEmpty(ItemStack item, int setCount) {
		@SuppressWarnings("deprecation")
		ItemStack itemCopy = new ItemStack(item.item, setCount, item.components.copy());
		itemCopy.setPopTime(item.getPopTime());
		return itemCopy;
	}
}
