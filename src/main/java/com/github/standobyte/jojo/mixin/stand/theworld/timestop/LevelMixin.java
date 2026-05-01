package com.github.standobyte.jojo.mixin.stand.theworld.timestop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopLevelTracker;

import net.minecraft.world.level.Level;

@Mixin(Level.class)
public class LevelMixin {
	
	@Inject(method = "shouldTickBlocksAt", at = @At("HEAD"), cancellable = true)
	public void cancelBlockTick(long chunkPos, CallbackInfoReturnable<Boolean> ci) {
		if (TimeStopLevelTracker.timeStops((Level) (Object) this)
				.anyMatch(timeStop -> timeStop.isInRange(chunkPos))) {
			ci.setReturnValue(false);
		}
	}
	
}
