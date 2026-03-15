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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxTileEntityMixin extends BlockEntity {

	public JukeboxTileEntityMixin(BlockEntityType<?> teType, BlockPos pos, BlockState blockState) {
		super(teType, pos, blockState);
	}

	@Shadow private ItemStack item;

	@Inject(method = "setTheItem", at = @At("HEAD"))
	public void jojo_ripples$onSetRecord(ItemStack record, CallbackInfo ci) {
		Level level = getLevel();
		if (level != null && !level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(record, level);
			if (tracker != null) {
				tracker.setAtBlockPos(record, this.getBlockPos(), level, KnownItemState.BLOCK_HAS_ITEM, trackerId -> 
						ItemTracking.hasTrackerId(this.item, trackerId));
			}
		}
	}
}
