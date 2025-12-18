package com.github.standobyte.jojo.util.mc;

import com.github.standobyte.jojo.mechanics.entity_like_player.playerwrapper.EntityAsPlayerWrapper;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

	public static void giveItemTo(LivingEntity entity, ItemStack item, boolean drop) {
		Level level = entity.level();
		if (!level.isClientSide() && !item.isEmpty()) {
			Player player = switch (entity) {
				case Player pl -> pl;
				case EntityAsPlayerWrapper ___ -> ___.asPlayer();
				default -> null;
			};
			if (player != null) {
				drop = !player.getInventory().add(item) && item.isEmpty();
			}
			if (drop) {
				level.addFreshEntity(dropAt(entity, item));
			}
		}
	}

	public static ItemEntity dropAt(LivingEntity entity, ItemStack item) {
		if (item.isEmpty()) {
			return null;
		}
		else {
			Level level = entity.level();
			ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getEyeY() - 0.3, entity.getZ(), item);
			itemEntity.setNoPickUpDelay();
			itemEntity.setTarget(entity.getUUID());
			return itemEntity;
		}
	}
}
