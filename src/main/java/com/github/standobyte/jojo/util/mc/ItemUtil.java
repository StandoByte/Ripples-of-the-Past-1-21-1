package com.github.standobyte.jojo.util.mc;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mechanics.entity_like_player.playerwrapper.EntityAsPlayerWrapper;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
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

	@Nullable
	public static ItemEntity dropItem(Entity entity, ItemStack item, boolean dropAround, boolean includeThrowerName) {
		if (item.isEmpty()) {
			return null;
		} else {
			double d0 = entity.getEyeY() - 0.3F;
			ItemEntity itementity = new ItemEntity(entity.level(), entity.getX(), d0, entity.getZ(), item);
			itementity.setPickUpDelay(40);
			if (includeThrowerName) {
				itementity.setThrower(entity);
			}

			RandomSource random = entity.getRandom();
			if (dropAround) {
				float f = random.nextFloat() * 0.5F;
				float f1 = random.nextFloat() * (float) (Math.PI * 2);
				itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), 0.2F, (double)(Mth.cos(f1) * f));
			} else {
				float f7 = 0.3F;
				float f8 = Mth.sin(entity.getXRot() * (float) (Math.PI / 180.0));
				float f2 = Mth.cos(entity.getXRot() * (float) (Math.PI / 180.0));
				float f3 = Mth.sin(entity.getYRot() * (float) (Math.PI / 180.0));
				float f4 = Mth.cos(entity.getYRot() * (float) (Math.PI / 180.0));
				float f5 = random.nextFloat() * (float) (Math.PI * 2);
				float f6 = 0.02F * random.nextFloat();
				itementity.setDeltaMovement(
						(double)(-f3 * f2 * f7) + Math.cos((double)f5) * (double)f6,
						(double)(-f8 * f7 + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F),
						(double)(f4 * f2 * f7) + Math.sin((double)f5) * (double)f6);
			}

			return itementity;
		}
	}
}
