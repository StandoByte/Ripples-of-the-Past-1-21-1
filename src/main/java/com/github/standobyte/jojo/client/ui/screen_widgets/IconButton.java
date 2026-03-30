package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends Button {
	public GuiIcon icon;

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress) {
		this(pX, pY, pWidth, pHeight, 
				icon, 
				pOnPress, null, CommonComponents.EMPTY);
	}

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip) {
		this(pX, pY, pWidth, pHeight, 
				icon, 
				pOnPress, pOnTooltip, CommonComponents.EMPTY);
	}

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
		super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
		this.icon = icon;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		if (icon != null) {
			float iconX = getX() + (width - icon.width) / 2;
			float iconY = getY() + (height - icon.height) / 2;
			icon.render(guiGraphics.pose(), iconX, iconY);
		}
	}

	@Override
	public void renderString(GuiGraphics guiGraphics, Font font, int color) {}
	
	
	public static final GuiIcon CHECKMARK = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/checkmark.png"), 16, 16);
	public static final GuiIcon CROSS = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/cross.png"), 16, 16);
	public static void renderCheckmarkOrCross(AbstractWidget button, boolean value, PoseStack poseStack) {
		int x = button.getRight() - 8;
		int y = button.getBottom() - 14;
		GuiIcon icon = value ? CHECKMARK : CROSS;
		icon.render(poseStack, x, y);
	}

}
