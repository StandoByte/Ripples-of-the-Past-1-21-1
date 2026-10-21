package com.github.standobyte.jojo.mixin.client.particle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mixininterface.ParticleExtension;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;

@Mixin(ParticleEngine.class)
public class ParticleTiedToEntityEngineMixin {
	
	@Inject(method = "Lnet/minecraft/client/particle/ParticleEngine;tickParticle("
			+ "Lnet/minecraft/client/particle/Particle;)V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/particle/Particle;tick()V",
			shift = At.Shift.AFTER))
	public void afterParticleTick(Particle particle, CallbackInfo ci) {
		ParticleExtension.onTickPost(particle);
	}
}
