package com.github.standobyte.jojo.mixin.damage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.network.s2c.DamageEventExtendedPacket;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(ServerLevel.class)
public class ServerLevelDamageBroadcast {

	@Inject(method = "Lnet/minecraft/server/level/ServerLevel;broadcastDamageEvent("
			+ "Lnet/minecraft/world/entity/Entity;"
			+ "Lnet/minecraft/world/damagesource/DamageSource;"
			+ ")V", at = @At("HEAD"), cancellable = true)
	public void sendExtendedPacket(Entity entity, DamageSource damageSource, CallbackInfo ci) {
		DamageSourceModified _damageSource = (DamageSourceModified) damageSource;
		boolean noHurtSound = _damageSource.jojo_ripples$getNoHurtSound();
		boolean noFlinchModelAnim = _damageSource.jojo_ripples$getNoFlinchModelAnim();
		float cameraTiltMultiplier = _damageSource.jojo_ripples$getCameraTiltMultiplier();
		if (noHurtSound || noFlinchModelAnim || cameraTiltMultiplier != 1) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new DamageEventExtendedPacket(entity, damageSource,
					noHurtSound, noFlinchModelAnim, cameraTiltMultiplier));
			ci.cancel();
		}
	}
}
