package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ModClientResources;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class CloseResources {

	@Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/font/providers/FreeTypeUtil;destroy()V"), require = 1)
	public void jojo_ripples$onClose(CallbackInfo ci) {
		for (AutoCloseable resource : ModClientResources.closeables) {
			try {
				resource.close();
			} catch (Exception e) {
				JojoMod.getLogger().error("Shutdown error!", e);
			}
		}
	}
}
