package com.github.standobyte.jojo.client.ui.utils;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class TextUtil {

	public static void drawRightAlignedString(GuiGraphics gui, Font font, String text, 
			int x, int y, int color, boolean shadow) {
		gui.drawString(font, text, x - font.width(text), y, color, shadow);
	}

	public static void drawRightAlignedString(GuiGraphics gui, Font font, Component text, 
			int x, int y, int color, boolean shadow) {
		gui.drawString(font, text, x - font.width(text), y, color, shadow);
	}
	
	public static final int TOOLTIP_MAX_WIDTH = 170;
	public static List<FormattedCharSequence> splitMultiLine(Font font, List<Component> message, int width) {
		return message.stream().flatMap(line -> font.split(line, width).stream()).toList();
	}
	
	public static final float MIN_TEXT_ALPHA = 1f / 63f;
	public static float fixAlpha(float alpha) {
		return Mth.clamp(alpha, MIN_TEXT_ALPHA, 1);
	}
}
