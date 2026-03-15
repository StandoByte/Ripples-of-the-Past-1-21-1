package com.github.standobyte.jojo.mixin.itemtracking.track.inventory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;
import com.github.standobyte.jojo.util.functions.ItemUtil;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin implements Container {
	@Shadow @Final public Player player;
	@Shadow @Final private List<NonNullList<ItemStack>> compartments;

	@Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
	public void jojo_ripples$onItemAddedToInv(int slot, ItemStack item, CallbackInfoReturnable<Boolean> ci) {
		Level level = player.level();
		if (!level.isClientSide() && Boolean.TRUE.equals(ci.getReturnValue())) {
			UUID prevItemTrackerId = ItemUtil.getFromEmptyItem(item, ModItemDataComponents.TRACKER_ID.get());
			ItemTracker itemTracker = ItemTracking.getItemTracker(prevItemTrackerId, level);
			if (itemTracker != null) {
				ItemStack trackedItem = ItemTracking.getItemWithTrackerInInventory(prevItemTrackerId, compartments.stream().flatMap(Collection::stream), level);
				if (trackedItem != null) {
					itemTracker.setAtEntity(trackedItem, player.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
							compartments.stream().flatMap(Collection::stream).anyMatch(ItemTracking.trackerIdCheck(trackerId)));
				}
			}
		}
	}

	@Inject(method = "setItem", at = @At("TAIL"))
	public void jojo_ripples$onItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
		Level level = player.level();
		if (!level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(item, level);
			if (tracker != null) {
				tracker.setAtEntity(item, player.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
						compartments.stream().flatMap(Collection::stream).anyMatch(ItemTracking.trackerIdCheck(trackerId)));
			}
		}
	}
}
