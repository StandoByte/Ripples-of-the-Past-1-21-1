package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class ClothesSetSearchField extends EditBox {

	public ClothesSetSearchField(Font font, 
			int x, int y, int width, int height, 
			Component message) {
		super(font, x, y, width, height, message);
	}
}
