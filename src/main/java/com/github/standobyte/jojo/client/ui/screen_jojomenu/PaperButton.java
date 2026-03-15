package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class PaperButton extends Button {

	public PaperButton(int x, int y, int width, int height, 
			Component message, OnPress onPress) {
		super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
	}

	public static final ResourceLocation ENABLED = JojoMod.resLoc("jojo_ripples/paper_style/button");
	public static final ResourceLocation DISABLED = JojoMod.resLoc("jojo_ripples/paper_style/button_disabled");
	public static final ResourceLocation ENABLED_FOCUSED = JojoMod.resLoc("jojo_ripples/paper_style/button_highlighted");
	public static final ResourceLocation FOCUS_HIGHLIGHT = JojoMod.resLoc("jojo_ripples/paper_style/button_overlay");
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		
		boolean enabled = this.active;
		boolean focused = this.isHoveredOrFocused();
		ResourceLocation sprite;
		if (enabled)	sprite = focused ? ENABLED_FOCUSED : ENABLED;
		else 			sprite = DISABLED;
		guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = getFGColor();
		this.renderString(guiGraphics, minecraft.font, (i | Mth.ceil(this.alpha * 255.0F) << 24) & 0xFF000000);
		if (isHovered() && enabled) {
			RenderSystem.enableBlend();
			ResourceLocation FOCUS_HIGHLIGHT = JojoMod.resLoc("jojo_ripples/paper_style/button_overlay");
			guiGraphics.blitSprite(FOCUS_HIGHLIGHT, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}
	}

	@Override
	protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int offset, int color) {
		int minX = this.getX() + offset;
		int minY = this.getY();
		int maxX = this.getX() + this.getWidth() - offset;
		int maxY = this.getY() + this.getHeight();
		int centerX = (minX + maxX) / 2;

		renderScrollingString(guiGraphics, font, this.getMessage(), 
				centerX, minX, minY, maxX, maxY, color, false);
	}

	public static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component text, 
			int centerX, int minX, int minY, int maxX, int maxY, int color, boolean shadow) {
		int i = font.width(text);
		int j = (minY + maxY - 9) / 2 + 1;
		int k = maxX - minX;
		if (i > k) {
			int l = i - k;
			double d0 = (double)Util.getMillis() / 1000.0;
			double d1 = Math.max((double)l * 0.5, 3.0);
			double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
			double d3 = Mth.lerp(d2, 0.0, (double)l);
			guiGraphics.enableScissor(minX, minY, maxX, maxY);
			guiGraphics.drawString(font, text, minX - (int)d3, j, color, shadow);
			guiGraphics.disableScissor();
		} else {
			int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
			FormattedCharSequence formattedcharsequence = text.getVisualOrderText();
			guiGraphics.drawString(font, formattedcharsequence, i1 - font.width(formattedcharsequence) / 2, j, color, shadow);
		}
	}

}
