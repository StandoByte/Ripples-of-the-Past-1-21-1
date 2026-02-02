package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixinSuperclass {
	@Shadow public abstract Level level();
	@Shadow public abstract Vec3 position();

	@Inject(method = "onAddedToLevel", at = @At("TAIL"))
	public void jojo_ripples$onAddedToWorld(CallbackInfo ci) {}
	
}
