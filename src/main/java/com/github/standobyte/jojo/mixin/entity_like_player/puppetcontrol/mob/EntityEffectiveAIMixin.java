package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.mob;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public class EntityEffectiveAIMixin {
	@Shadow private Level level;

	@Inject(method = "isEffectiveAi", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$clientSideEffectiveAIFlag(CallbackInfoReturnable<Boolean> ci) {
		if (level.isClientSide() && ClientEntityController.isBeingControlledByClient((Entity) (Object) this)) {
			ci.setReturnValue(true);
		}
	}

	@Inject(method = "isControlledByLocalInstance", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$sidedControlFlag(CallbackInfoReturnable<Boolean> ci) {
		Entity thisEntity = (Entity) (Object) this;
		if (level.isClientSide()) {
			if (ClientEntityController.isBeingControlledByClient(thisEntity)) {
				ci.setReturnValue(true);
			}
		}
		else {
			EntityComponentController controller = EntityComponentController.getCurrentController(thisEntity);
			if (controller != null && controller.suppressControlledEntity()) {
				ci.setReturnValue(false);
			}
		}
	}
}
