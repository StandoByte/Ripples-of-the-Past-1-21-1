package com.github.standobyte.jojo.client.ui.utils;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.components.Button;

@FunctionalInterface
public interface OnButtonClick extends Button.OnPress {
	void onPress(Button button, int mouseButton);
	
	@Override
	default void onPress(Button button) {
		onPress(button, InputConstants.MOUSE_BUTTON_LEFT);
	}
}
