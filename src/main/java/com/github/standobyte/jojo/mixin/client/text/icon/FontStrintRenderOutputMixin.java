package com.github.standobyte.jojo.mixin.client.text.icon;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.textsymbols.IconSymbols;

import net.minecraft.client.gui.Font;

@Mixin(Font.StringRenderOutput.class)
public class FontStrintRenderOutputMixin {
	@Shadow @Final private float dimFactor;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void jojo_ripples$saveDimFactor(CallbackInfo ci) {
		IconSymbols._curDimFactor = this.dimFactor;
	}
}
