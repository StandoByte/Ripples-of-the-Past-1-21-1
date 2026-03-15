package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.world.entity.Entity;

// it should've been in MouseHandler#turnPlayer, redirecting Entity#turn call on minecraft.player, 
// but i should avoid redirects at all costs, since they can collide with mixins from other mods
@Mixin(Entity.class)
public class EntityMixinMouseTurn {

	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$turnRemoteStand(double yRot, double xRot, CallbackInfo ci) {
		if ((Object) this == ClientProxy.getClientPlayer()) {
			var controller = ClientEntityController.getInstance();
			if (controller != null && controller.turn(yRot, xRot)) {
				ci.cancel();
			}
		}
	}

}
