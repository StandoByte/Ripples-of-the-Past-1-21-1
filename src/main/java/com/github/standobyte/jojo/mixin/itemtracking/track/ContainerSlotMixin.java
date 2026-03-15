package com.github.standobyte.jojo.mixin.itemtracking.track;

import java.util.stream.IntStream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(Slot.class)
public abstract class ContainerSlotMixin {
	@Shadow @Final public Container container;

	@Inject(method = "set", at = @At("TAIL"))
	public void jojo_ripples$onItemSetToSlot(ItemStack stack, CallbackInfo ci) {
		if (container instanceof Entity entity) {
			Level level = entity.level();
			if (!level.isClientSide()) {
				ItemTracker tracker = ItemTracking.getItemTracker(stack, level);
				if (tracker != null) {
					tracker.setAtEntity(stack, entity.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
							IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
							.anyMatch(ItemTracking.trackerIdCheck(trackerId)));
				}
			}
		}
		else if (container instanceof BlockEntity tileEntity) {
			Level level = tileEntity.getLevel();
			if (level != null && !level.isClientSide()) {
				ItemTracker tracker = ItemTracking.getItemTracker(stack, level);
				if (tracker != null) {
					tracker.setAtBlockPos(stack, tileEntity.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM, trackerId -> 
							IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
							.anyMatch(ItemTracking.trackerIdCheck(trackerId)));
				}
			}
		}
		else if (container instanceof Inventory playerInventory) {
			Player player = playerInventory.player;
			Level level = player.level();
			if (!level.isClientSide()) {
				ItemTracker tracker = ItemTracking.getItemTracker(stack, level);
				if (tracker != null) {
					tracker.setAtEntity(stack, player.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
							IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem)
							.anyMatch(ItemTracking.trackerIdCheck(trackerId)));
				}
			}
		}
	}
}
