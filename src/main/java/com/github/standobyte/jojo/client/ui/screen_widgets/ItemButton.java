package com.github.standobyte.jojo.client.ui.screen_widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {
	public ItemStack item;

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress) {
		this(pX, pY, pWidth, pHeight, 
				item, 
				pOnPress, null, CommonComponents.EMPTY);
	}

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress, Tooltip pOnTooltip) {
		this(pX, pY, pWidth, pHeight, 
				item, 
				pOnPress, pOnTooltip, CommonComponents.EMPTY);
	}

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
		super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
		this.item = item;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		int itemX = getX() + (width - 16) / 2;
		int itemY = getY() + (height - 16) / 2;
		guiGraphics.renderItem(item, itemX, itemY);
		guiGraphics.renderItemDecorations(Minecraft.getInstance().font, item, itemX, itemY);
	}

	@Override
	public void renderString(GuiGraphics guiGraphics, Font font, int color) {}

}
