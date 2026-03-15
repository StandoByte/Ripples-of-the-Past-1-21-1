package com.github.standobyte.jojo.mixin.possession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_possessionv2.LivingComponentPossession;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

	public LivingEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}
	
	@Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$possessingNotPickable(CallbackInfoReturnable<Boolean> ci) {
		if (LivingComponentPossession.getEntityPossessedBy(this) != null) {
			ci.setReturnValue(false);
		}
	}
	
	@Inject(method = "canBeSeenByAnyone", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$possessingNotVisibleToMobs(CallbackInfoReturnable<Boolean> ci) {
		if (LivingComponentPossession.getEntityPossessedBy(this) != null) {
			ci.setReturnValue(false);
		}
	}
	
	@Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$possessingNotPushable(CallbackInfoReturnable<Boolean> ci) {
		if (LivingComponentPossession.getEntityPossessedBy(this) != null) {
			ci.setReturnValue(false);
		}
	}

	@Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$possessionCancelPush(CallbackInfo ci) {
		if (LivingComponentPossession.getEntityPossessedBy(this) != null) {
			ci.cancel();
		}
	}
}
