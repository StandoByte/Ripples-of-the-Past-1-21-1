package com.github.standobyte.jojo.client.textsymbols.sprite;

import java.util.function.Function;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class IconGlyphInfo implements GlyphInfo {
	protected ResourcePathChecker spriteFileCheck;
	public GuiIcon icon;
	public float width;
	public float height;
	public float left;
	public float up;
	public float offset;
	
	public IconGlyphInfo(GuiIcon icon, float width, float height) {
		this(icon, width, height, 0, (7 - height) / 2f, 1);
	}
	
	public IconGlyphInfo(GuiIcon icon, float width, float height, float left, float up, float offset) {
		this.icon = icon;
		this.width = width;
		this.height = height;
		this.left = left;
		this.up = up;
		this.offset = offset;
	}

	@Override
	public float getAdvance() {
		return width + offset;
	}
	
	public boolean exists() {
		if (spriteFileCheck == null) {
			spriteFileCheck = ResourcePathChecker.getOrCreate(icon.file);
		}
		return spriteFileCheck.resourceExists();
	}

	@Override
	public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphProvider) {
		GlyphRenderTypes renderTypes = GlyphRenderTypes.createForColorTexture(icon.file);
		return new IconBakedGlyph(renderTypes, 
				icon.minU, icon.minU + icon.widthU, icon.minV, icon.minV + icon.heightV, 
				left, left + width, up, up + height);
	}

}
