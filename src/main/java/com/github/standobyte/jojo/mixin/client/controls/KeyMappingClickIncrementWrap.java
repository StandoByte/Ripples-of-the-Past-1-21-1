package com.github.standobyte.jojo.mixin.client.controls;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.standobyte.jojo.client.input.VanillaKeyMappingExtended;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public class KeyMappingClickIncrementWrap {

	@WrapWithCondition(
			method = "Lnet/minecraft/client/KeyMapping;"
			+ "click("
			+ "Lcom/mojang/blaze3d/platform/InputConstants$Key;"
			+ ")V",
			at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD /* LlamaLad is the GOAT */, 
			target = "Lnet/minecraft/client/KeyMapping;clickCount:I"))
	private static boolean onIncrement(KeyMapping keybind, int incrementedValue) {
		return !(keybind instanceof VanillaKeyMappingExtended extended) || extended.onClickCountIncrement();
	}
}
