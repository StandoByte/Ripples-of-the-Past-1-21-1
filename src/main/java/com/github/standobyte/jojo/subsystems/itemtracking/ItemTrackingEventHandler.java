package com.github.standobyte.jojo.subsystems.itemtracking;

import java.util.function.Predicate;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ItemTrackingEventHandler {

	@SubscribeEvent
	public static void trackEntityItem(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		Level level = entity.level();
		if (!level.isClientSide()) {
			if (entity instanceof AbstractArrow arrow) {
				ItemStack item = arrow.getPickupItemStackOrigin();
				ItemTracker tracker = ItemTracking.getItemTracker(item, level);
				if (tracker != null) {
					tracker.setAtEntity(item, entity.getId(), level, KnownItemState.ENTITY_IS_ITEM, null);
				}
			}
		}
	}

	
	private static final Predicate<ItemStack> PREFER_TRACKED_PROJECTILES = ItemTracking::isProbablyTracked;
	@SubscribeEvent
	public static void preferTrackedProjectiles(LivingGetProjectileEvent event) {
		ItemStack curAmmo = event.getProjectileItemStack();
		if (PREFER_TRACKED_PROJECTILES.test(curAmmo)) {
			return;
		}

		LivingEntity entity = event.getEntity();
		if (entity instanceof Player player) {
			ItemStack weaponItemStack = event.getProjectileWeaponItemStack();
			if (weaponItemStack.getItem() instanceof ProjectileWeaponItem weaponItem) {
				Predicate<ItemStack> predicate = weaponItem.getAllSupportedProjectiles(weaponItemStack)
						.and(PREFER_TRACKED_PROJECTILES);
				
				Inventory inventory = player.getInventory();
				for (int i = 0; i < inventory.getContainerSize(); ++i) {
					ItemStack invTrackedAmmo = inventory.getItem(i);
					if (predicate.test(invTrackedAmmo)) {
						event.setProjectileItemStack(invTrackedAmmo);
						return;
					}
				}
			}
		}
	}
}
