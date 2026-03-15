package com.github.standobyte.jojo.mixin.possession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_possessionv2.LivingComponentPossession;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "touch", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$possessionCancelPickUp(Entity entity, CallbackInfo ci) {
		if (LivingComponentPossession.isPossessingSomeone(this)) {
			ci.cancel();
		}
	}
}
