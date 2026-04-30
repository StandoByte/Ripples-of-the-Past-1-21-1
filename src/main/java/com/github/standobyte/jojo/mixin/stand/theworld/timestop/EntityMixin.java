package com.github.standobyte.jojo.mixin.stand.theworld.timestop;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public class EntityMixin {

	// Fixes frozen slimes attacking when you touch them.
	// Injecting into Mob#isNoAi() instead would cause the no AI flag to be saved as true in NBT
	@Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
	public void noAIInTimeStop(CallbackInfoReturnable<Boolean> ci) {
		if (TimeStopEffect.getIsFrozenInTime((Entity) (Object) this)) {
			ci.setReturnValue(false);
		}
	}

	@ModifyVariable(method = "updateFluidHeightAndDoFluidPushing()V", 
			at = @At(value = "STORE", ordinal = 0),
			ordinal = 1 /* tried a slice, didn't seem to work */)
	public Vec3 cancelFluidPush(Vec3 flowVec) {
		if (TimeStopEffect.getIsInsideTimeStop((Entity) (Object) this)) {
			return Vec3.ZERO;
		}
		return flowVec;
	}
}
