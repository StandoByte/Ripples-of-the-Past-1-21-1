package com.github.standobyte.jojo.client.textsymbols.sprite;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.textsymbols.IconSymbols;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

public class IconGlyphsCache {
	public static Char2ObjectMap<IconGlyphInfo> _glyphsByIndex = new Char2ObjectArrayMap<>();

	public static char makeCharCodeFor(IconGlyphInfo glyph) {
		IconSymbols.makeSureThisClassLoadsFirst();
		char index = (char) _glyphsByIndex.size();
		_glyphsByIndex.put(index, glyph);
		return indexToCharCode(index);
	}


	protected static final char _UTF_16_PCA = 0xEAFA; // private code area: 0xE000..0xF8FF, we'll start somewhere in the middle

	public static char indexToCharCode(char index) {
		return (char) (index + _UTF_16_PCA);
	}

	@ApiStatus.Internal
	@Nullable
	public static IconGlyphInfo get(char character) {
		int index = character - _UTF_16_PCA;
		if (index < 0) return null;
		return _glyphsByIndex.get((char) index);
	}
}
