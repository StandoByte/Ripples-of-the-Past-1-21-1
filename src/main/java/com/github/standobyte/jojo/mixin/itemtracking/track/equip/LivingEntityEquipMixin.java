package com.github.standobyte.jojo.mixin.itemtracking.track.equip;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mixin.entity_like_player.npc.InventoryAccessor;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;
import com.github.standobyte.jojo.util.functions.ItemUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEquipMixin extends Entity {

	public LivingEntityEquipMixin(EntityType<?> pType, Level pLevel) {
		super(pType, pLevel);
	}

	@Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);

	@Inject(method = "onEquipItem", at = @At("HEAD"))
	public void jojo_ripples$onItemEquip(EquipmentSlot pSlot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide()) {
			UUID newItemTrackerId = ItemUtil.getFromEmptyItem(newItem, ModItemDataComponents.TRACKER_ID.get());
			if (newItemTrackerId != null) {
				ItemTracker tracker = ItemTracking.getItemTracker(newItemTrackerId, level);
				if (tracker != null) {
					Predicate<UUID> itemStillThereCheck;
					if (this.getType() == EntityType.PIGLIN && newItem.isPiglinCurrency()) {
						itemStillThereCheck = null; // keep the tracker after the piglin puts away the gold ingot as the result of bartering (it goes into the void or smth)
					}
					else if (((Entity) this) instanceof Player player) {
						Inventory inventory = player.getInventory();
						List<NonNullList<ItemStack>> compartments = ((InventoryAccessor) inventory).getCompartments();
						itemStillThereCheck = trackerId -> 
								compartments.stream().flatMap(Collection::stream).anyMatch(ItemTracking.trackerIdCheck(trackerId));
					}
					else {
						itemStillThereCheck = trackerId -> 
								ItemTracking.trackerIdCheck(trackerId).test(this.getItemBySlot(pSlot));
					}
					tracker.setAtEntity(newItem, this.getId(), level, KnownItemState.ENTITY_HAS_ITEM, itemStillThereCheck);
				}
			}
		}
	}

	@Inject(method = "take", at = @At("HEAD"))
	public void jojo_ripples$onTakeWithoutEquipping(Entity entity, int amount, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide() && amount == 1 && (entity instanceof ItemEntity || entity instanceof AbstractArrow)) {
			ItemStack oldItem = switch (entity) {
				case ItemEntity itemEntity -> itemEntity.getItem();
				case AbstractArrow arrowEntity -> arrowEntity.getPickupItemStackOrigin();
				default -> ItemStack.EMPTY;
			};
			UUID newItemTrackerId = ItemUtil.getFromEmptyItem(oldItem, ModItemDataComponents.TRACKER_ID.get());
			if (newItemTrackerId != null) {
				ItemTracker tracker = ItemTracking.getItemTracker(newItemTrackerId, level);
				if (tracker != null) {
					if (this instanceof InventoryCarrier THIS_SHIT_HAS_TENS_OF_DIFFERENT_INVENTORY_IMPLEMENTATIONS_FFS) {
						SimpleContainer inventory = THIS_SHIT_HAS_TENS_OF_DIFFERENT_INVENTORY_IMPLEMENTATIONS_FFS.getInventory();
						List<ItemStack> items = inventory.getItems();
						ItemStack item = ItemTracking.getItemWithTrackerInInventory(newItemTrackerId, items.stream(), level);
						if (item != null) {
							tracker.setAtEntity(item, this.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
									items.stream().anyMatch(ItemTracking.trackerIdCheck(trackerId)));
						}
					}
				}
			}
		}
	}
}
