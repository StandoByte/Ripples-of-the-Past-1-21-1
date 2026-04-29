package com.github.standobyte.jojo.mixin.stand.theworld.timestop.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojoimpl.stands.theworld.timestop.client.TimeStopClientState;

import net.minecraft.client.renderer.texture.TextureManager;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void cancelTick(CallbackInfo ci) {
		if (TimeStopClientState.isTimeStopped) {
			ci.cancel();
		}
	}

}
