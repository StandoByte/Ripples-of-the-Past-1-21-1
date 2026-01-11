package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.mechanics.KnockbackCollisionImpact;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Entity.class)
public abstract class KnockbackEntityCollision {
	
	@Shadow public Level level;
	private KnockbackCollisionImpact jojo_ripples$kbCollision;

	@Inject(method = "collide", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$collideBreakBlocks(Vec3 movementVec, CallbackInfoReturnable<Vec3> ci) {
		if (jojo_ripples$kbCollision == null) {
			jojo_ripples$kbCollision = KnockbackCollisionImpact.getHandler((Entity) (Object) this);
		}
		if (jojo_ripples$kbCollision != null) {
			jojo_ripples$kbCollision.collideBreakBlocks(movementVec, ci.getReturnValue(), level);
		}
	}
	
    @Shadow protected abstract Vec3 collide(Vec3 pVec);
}
