package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import java.util.Map;
import java.util.Queue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;
import com.google.common.collect.EvictingQueue;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;

//TODO tick instances of ItemPickupParticle
@Mixin(ParticleEngine.class)
public class ParticleManagerMixin {
	@Shadow @Final private Map<ParticleRenderType, Queue<Particle>> particles;
	@Shadow @Final private Queue<Particle> particlesToAdd;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void particleCancelTick(CallbackInfo ci) {
		if (TimeStopClientState.isTimeStopped) {
	        addParticlesInStoppedTime();
			ci.cancel();
		}
	}
	
	@Unique
	private void addParticlesInStoppedTime() {
		if (TimeStopClientState.canSeeInStoppedTime) {
			Particle particle;
			if (!this.particlesToAdd.isEmpty()) {
				while ((particle = this.particlesToAdd.poll()) != null) {
					if (TimeStopClientState.addParticleInStoppedTime(particle)) {
						this.particles.computeIfAbsent(particle.getRenderType(), 
								renderType -> EvictingQueue.create(16384)).add(particle);
					}
				}
			}
		}
	}

	@ModifyVariable(method = "render("
			+ "Lnet/minecraft/client/renderer/LightTexture;"
			+ "Lnet/minecraft/client/Camera;"
			+ "FLnet/minecraft/client/renderer/culling/Frustum;"
			+ "Ljava/util/function/Predicate;"
			+ ")V", 
			remap = false, at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float particleChangePartialTick(float partialTick) {
		if (TimeStopClientState.isTimeStopped) {
			return 1;
		}
		return partialTick;
	}

}
