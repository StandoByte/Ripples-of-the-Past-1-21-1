package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public interface IJojoMenuTab {
	void renderIcon(GuiGraphics guiGraphics, int x, int y);
	Component getName();
	Tab getTabToOpen();
	
	default boolean onClick(Minecraft mc, @Nullable Screen curScreen) {
		Tab tab = getTabToOpen();
		return tab != null && tab.onTabClick(mc, curScreen);
	}
}
