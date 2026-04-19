package com.github.standobyte.jojo.client.ui.screen_widgets.utils;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

public class ButtonDecoration {
	protected GuiIcon icon;
	protected ItemStack item;
	
	protected ButtonDecoration(GuiIcon icon, ItemStack item) {
		this.icon = icon;
		this.item = item;
	}

	public static ButtonDecoration icon(GuiIcon icon) {
		return new ButtonDecoration(icon, null);
	}
	
	public static ButtonDecoration item(ItemStack item) {
		return new ButtonDecoration(null, item);
	}

	public void render(AbstractWidget parentButton, GuiGraphics guiGraphics, 
			int mouseX, int mouseY, float partialTick) {
		if (icon != null) {
			float iconX = parentButton.getX() + (parentButton.getWidth() - icon.width) / 2;
			float iconY = parentButton.getY() + (parentButton.getHeight() - icon.height) / 2;
			icon.render(guiGraphics.pose(), iconX, iconY);
		}
		else if (item != null) {
			int itemX = parentButton.getX() + (parentButton.getWidth() - 16) / 2;
			int itemY = parentButton.getY() + (parentButton.getHeight() - 16) / 2;
			guiGraphics.renderItem(item, itemX, itemY);
			guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, itemX, itemY);
		}
	}

}
