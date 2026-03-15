package com.github.standobyte.jojo.client.textsymbols;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

public class IconSymbols {
	// EAFA
	public static final char LMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/left_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
	// EAFB
	public static final char RMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/right_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
	// EAFC
	public static final char MMB_CLICK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/middle_click.png"), 3, 0, 10, 16, 16, 16), 5, 8));
	// EAFD
	public static final char LMB_CLICK_LARGE = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/left_click.png"), 3, 0, 10, 16, 16, 16), 10, 16));
	// EAFE
	public static final char RMB_CLICK_LARGE = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/right_click.png"), 3, 0, 10, 16, 16, 16), 10, 16));
	// EAFF
	public static final char MMB_CLICK_LARGE = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/middle_click.png"), 3, 0, 10, 16, 16, 16), 10, 16));

	// EB00
	public static final char CHECKMARK = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/checkmark.png"), 0, 0, 16, 16, 16, 16), 16, 16));
	// EB01
	public static final char CROSS = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/cross.png"), 0, 0, 16, 16, 16, 16), 16, 16));
	// EB02
	public static final char WARNING = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/warning.png"), 0, 0, 16, 16, 16, 16), 16, 16));

	// EB03
	public static final char TIME = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/time.png"), 0, 0, 9, 9, 9, 9), 9, 9));
	// EB04
	public static final char VOLUME = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/volume.png"), 0, 0, 9, 9, 9, 9), 9, 9));
	// EB05
	public static final char HEALTH = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/health.png"), 0, 0, 9, 9, 9, 9), 9, 9));
	// EB06
	public static final char DAMAGE = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/damage.png"), 0, 0, 9, 9, 9, 9), 9, 9));
	// EB07
	public static final char ARMOR = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/armor.png"), 0, 0, 9, 9, 9, 9), 9, 9));
	// EB08
	public static final char STAND_EXP = IconGlyphsCache.makeCharCodeFor(new IconGlyphInfo(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/stand_exp.png"), 0, 0, 10, 10, 10, 10), 10, 10));
	
	
	public static boolean canRecolor = false;
	/* saves the vanilla variable that controls the shadow dimness, so that
	 * the icon glyph shadows render correctly when canRecolor is false
	 */
	@ApiStatus.Internal public static float _curDimFactor = 1;
	
	public static boolean spriteExists(char character) {
		IconGlyphInfo glyph = IconGlyphsCache.get(character);
		return glyph != null && glyph.exists();
	}
	
	public static void makeSureThisClassLoadsFirst() {}
	
}
