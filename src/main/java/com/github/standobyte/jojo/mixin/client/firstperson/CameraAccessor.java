package com.github.standobyte.jojo.mixin.client.firstperson;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.class)
public interface CameraAccessor {
	@Invoker("Lnet/minecraft/client/Camera;setPosition(Lnet/minecraft/world/phys/Vec3;)V") void invokeSetPosition(Vec3 position);
}
