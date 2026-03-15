package com.github.standobyte.jojo.mixin.client.controls;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.event.client.ModClientEventHooks;

import net.minecraft.client.KeyboardHandler;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

	@Inject(method = "keyPress", at = @At(value = "FIELD", target = 
			"Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", opcode = Opcodes.GETFIELD, ordinal = 0), 
			cancellable = true)
	public void jojo_ripples$onKeyPressPre(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
		if (ModClientEventHooks.onKeyboardInputPre(key, scanCode, action, modifiers)) {
			ci.cancel();
		}
	}
}
