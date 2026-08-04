package com.github.standobyte.jojo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

public interface CustomRenderCreativeTab {
	void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, 
			CreativeModeInventoryScreen screen, int rowScrolled);
}
