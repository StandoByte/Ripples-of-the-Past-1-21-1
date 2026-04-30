package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

@Mixin(Camera.class)
public class CameraPartialTickMixin {

	@ModifyVariable(method = "setup", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float changePartialTickOnSetup(float partialTick, 
			BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick_) {
		if (TimeStopEffect.getIsFrozenInTime(entity)) {
			return 1;
		}
		return partialTick;
	}

}
