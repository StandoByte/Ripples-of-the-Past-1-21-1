package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

@Mixin(LookControl.class)
public class MobAILookMixin {
	@Shadow @Final protected Mob mob;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelAILook(CallbackInfo ci) {
		// the mixin is only applied on the client side
		if (/*mob.level().isClientSide() && */ClientEntityController.isBeingControlledByClient(mob)) {
			ci.cancel();
		}
	}
}
