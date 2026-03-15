package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow private boolean renderBlockOutline;

    @Inject(method = "shouldRenderBlockOutline", 
    		at = @At(value = "RETURN"), 
    		slice = @Slice(from = @At(
    				value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getCameraEntity()V")), 
    		cancellable = true)
    private void jojo_ripples$blockOutline(CallbackInfoReturnable<Boolean> ci) {
		if (renderBlockOutline) {
			ClientEntityController controller = ClientEntityController.getInstance();
			if (controller != null && controller.shouldRenderBlockOutline()) {
				ci.setReturnValue(true);
			}
		}
	}
}
