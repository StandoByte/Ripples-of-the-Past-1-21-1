package com.github.standobyte.jojo.client.ui.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

public enum Alignment {
	LEFT,
	CENTER,
	RIGHT;
	
	public int getOffset(Font font, FormattedText line) {
		return switch (this) {
			case LEFT -> 0;
			default -> getOffset(font.width(line));
		};
	}
	
	public int getOffset(int lineWidth) {
		return switch (this) {
			case LEFT -> 0;
			case CENTER -> -lineWidth / 2;
			case RIGHT -> -lineWidth;
		};
	}
}
