package com.github.standobyte.jojo.mixin.client.text.icon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.mojang.blaze3d.font.GlyphInfo;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

@Mixin(FontSet.class)
public class FontSetMixin {

	@Inject(method = "computeGlyphInfo", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$customIconGlyphInfo(int character, CallbackInfoReturnable<FontSet.GlyphInfoFilter> ci) {
		GlyphInfo info = IconGlyphsCache.get((char) character);
		if (info != null) {
			ci.setReturnValue(new FontSet.GlyphInfoFilter(info, info));
		}
	}

	@Inject(method = "computeBakedGlyph", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$customIconGlyph(int character, CallbackInfoReturnable<BakedGlyph> ci) {
		GlyphInfo info = IconGlyphsCache.get((char) character);
		if (info != null) {
			ci.setReturnValue(info.bake(sheetGlyphInfo -> { throw new UnsupportedOperationException(); }));
		}
	}
}
