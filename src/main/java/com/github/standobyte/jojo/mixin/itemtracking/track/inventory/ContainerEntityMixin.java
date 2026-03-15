package com.github.standobyte.jojo.mixin.itemtracking.track.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ContainerEntity.class)
public abstract interface ContainerEntityMixin {

	@Shadow NonNullList<ItemStack> getItemStacks();

	@Inject(method = "setChestVehicleItem", at = @At("TAIL"))
	public default void jojo_ripples$onItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
		Entity thisAsEntity = (Entity) this;
		Level level = thisAsEntity.level();
		if (!level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(item, level);
			if (tracker != null) {
				tracker.setAtEntity(item, thisAsEntity.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
						this.getItemStacks().stream().anyMatch(ItemTracking.trackerIdCheck(trackerId)));
			}
		}
	}
}
