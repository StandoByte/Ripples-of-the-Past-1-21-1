package com.github.standobyte.jojo.mixin.possession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_possessionv2.LivingComponentPossession;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class EntityMixin {
	
	@Shadow private Vec3 position;
	
	@Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$zeroVolumeBBWhilePossessing(CallbackInfoReturnable<AABB> ci) {
		if (LivingComponentPossession.isPossessingSomeone((Entity) (Object) this)) {
			ci.setReturnValue(new AABB(position, position));
		}
	}
	
}
