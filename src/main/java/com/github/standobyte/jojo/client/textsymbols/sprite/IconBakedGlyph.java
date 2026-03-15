package com.github.standobyte.jojo.client.textsymbols.sprite;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class IconBakedGlyph extends BakedGlyph {
	protected final float u0;
	protected final float u1;
	protected final float v0;
	protected final float v1;
	protected final float left;
	protected final float right;
	protected final float up;
	protected final float down;

	public IconBakedGlyph(GlyphRenderTypes renderTypes, 
			float u0, float u1, float v0, float v1, 
			float left, float right, float up, float down) {
		super(renderTypes, u0, u1, v0, v1, left, right, up, down);
		this.u0 = u0;
		this.u1 = u1;
		this.v0 = v0;
		this.v1 = v1;
		this.left = left;
		this.right = right;
		this.up = up;
		this.down = down;
	}

	public void render(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer buffer, 
			float red, float green, float blue, float alpha, int packedLight) {
		if (!IconSymbols.canRecolor) {
			red = IconSymbols._curDimFactor;
			green = IconSymbols._curDimFactor;
			blue = IconSymbols._curDimFactor;
		}
		super.render(italic, x, y, matrix, buffer, red, green, blue, alpha, packedLight);
	}
	
}
