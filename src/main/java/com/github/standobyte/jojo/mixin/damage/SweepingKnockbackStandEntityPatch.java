package com.github.standobyte.jojo.mixin.damage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class SweepingKnockbackStandEntityPatch {

	@WrapWithCondition(method = "attack", at = @At(
			value = "INVOKE", 
			target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V", ordinal = 1))
	public boolean excludeStandEntitiesFromSweepingKnockback(LivingEntity sweepingTarget, 
			double strength, double x, double z) {
		return (!(sweepingTarget instanceof StandEntity));
	}
}
