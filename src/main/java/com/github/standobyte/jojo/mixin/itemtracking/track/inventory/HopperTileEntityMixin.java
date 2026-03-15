package com.github.standobyte.jojo.mixin.itemtracking.track.inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(HopperBlockEntity.class)
public abstract class HopperTileEntityMixin extends RandomizableContainerBlockEntity {

	protected HopperTileEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Shadow protected abstract NonNullList<ItemStack> getItems();

	@Inject(method = "setItem", at = @At("TAIL"))
	public void jojo_ripples$onItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
		Level level = getLevel();
		if (level != null && !level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(item, level);
			if (tracker != null) {
				ItemStack trackedItem = ItemTracking.getItemWithTrackerInInventory(item, getItems().stream(), level);
				if (trackedItem != null) {
					tracker.setAtBlockPos(trackedItem, this.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM, trackerId -> 
							getItems().stream().anyMatch(ItemTracking.trackerIdCheck(trackerId)));
				}
			}
		}
	}
}
