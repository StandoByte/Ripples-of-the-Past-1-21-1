package com.github.standobyte.jojo.mixin.stand.theworld.timestop;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopLevelTracker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;

@Mixin(CollectingNeighborUpdater.class)
public class DelayBlockUpdateMixin {
	@Shadow @Final private Level level;
	@Shadow int count;

	@Inject(method = "addAndRun", at = @At("HEAD"), cancellable = true)
	public void onUpdateAdd(BlockPos pos, CollectingNeighborUpdater.NeighborUpdates updates, CallbackInfo ci) {
		if (count == 0) {
			TimeStopLevelTracker tracker = TimeStopLevelTracker.get(level);
			if (tracker != null && tracker.isTimeStoppedAt(pos)
					&& tracker.delayBlockUpdate(pos, updates)) {
				ci.cancel();
			}
		}
	}
	
}
