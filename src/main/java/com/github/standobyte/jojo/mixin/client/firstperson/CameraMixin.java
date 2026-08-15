package com.github.standobyte.jojo.mixin.client.firstperson;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.event.client.ModClientEventHooks;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow private Vec3 position = Vec3.ZERO;

	@Shadow protected abstract void setPosition(Vec3 pos);

	@Inject(method = "Lnet/minecraft/client/Camera;setup("
			+ "Lnet/minecraft/world/level/BlockGetter;"
			+ "Lnet/minecraft/world/entity/Entity;"
			+ "ZZF)V", 
			at = @At("TAIL"))
	public void setupCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
		ModClientEventHooks.afterCameraSetup((Camera) (Object) this, detached, thirdPersonReverse);
	}

}
