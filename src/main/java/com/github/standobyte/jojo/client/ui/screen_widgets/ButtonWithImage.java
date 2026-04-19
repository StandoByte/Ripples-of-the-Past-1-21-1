package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ButtonWithImage extends Button {
	public ButtonDecoration icon;
	public WidgetSprites sprites = AbstractButton.SPRITES;

	public ButtonWithImage(int pX, int pY, int pWidth, int pHeight, 
			ButtonDecoration icon, 
			Button.OnPress pOnPress) {
		this(pX, pY, pWidth, pHeight, 
				icon, 
				pOnPress, null, CommonComponents.EMPTY);
	}

	public ButtonWithImage(int pX, int pY, int pWidth, int pHeight, 
			ButtonDecoration icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip) {
		this(pX, pY, pWidth, pHeight, 
				icon, 
				pOnPress, pOnTooltip, CommonComponents.EMPTY);
	}

	public ButtonWithImage(int pX, int pY, int pWidth, int pHeight, 
			ButtonDecoration icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
		super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
		this.icon = icon;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		ResourceLocation sprite = pickSprite();
		guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = getFGColor();
		this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
        
		if (icon != null) {
			icon.render(this, guiGraphics, mouseX, mouseY, partialTick);
		}
	}
	
	protected ResourceLocation pickSprite() {
		return sprites.get(this.active, this.isHoveredOrFocused());
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
