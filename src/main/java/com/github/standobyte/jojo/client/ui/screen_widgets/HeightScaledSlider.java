package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class HeightScaledSlider extends AbstractSliderButton {
	public int defaultWidth = 200;
	public int defaultHeight = 20;
	public int handleWidth = 8;

	public ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/slider.png");
	public ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/slider_highlighted.png");
	public ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/slider_handle.png");
	public ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/slider_handle_highlighted.png");

	public HeightScaledSlider(int pX, int pY, int pWidth, int pHeight, Component pMessage, double pValue) {
		super(pX, pY, pWidth, pHeight, pMessage, pValue);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		int x = this.getX();
		int y = this.getY();
		int width = this.getWidth();
		int height = this.getHeight();
		int handleX = x + (int)(this.value * (double)(width - handleWidth));

		boolean canChangeValue = /*this.canChangeValue*/true;
		ResourceLocation bgSprite = this.isFocused() && !canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
		ResourceLocation handleSprite = !this.isHovered && !canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
		
		// listen, if it works, it works
		BlitFloat.blit(guiGraphics.pose(), minecraft, bgSprite, 
				x, y, width / 2, height / 2, 0, 
				0, 0, width / 2, height / 2, defaultWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, bgSprite, 
				x, y + height / 2, width / 2, height - height / 2, 0, 
				0, defaultHeight - (height - height / 2), width / 2, height - height / 2, defaultWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, bgSprite, 
				x + width / 2, y, width / 2, height / 2, 0, 
				width / 2, 0, defaultWidth - (width - width / 2), height / 2, defaultWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, bgSprite, 
				x + width / 2, y + height / 2, width / 2, height - height / 2, 0, 
				width / 2, defaultHeight - (height - height / 2), width - width / 2, height - height / 2, defaultWidth, defaultHeight, BlitFloat.NO_TINT);

		BlitFloat.blit(guiGraphics.pose(), minecraft, handleSprite, 
				handleX, y, handleWidth / 2, height / 2, 0, 
				0, 0, handleWidth / 2, height / 2, handleWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, handleSprite, 
				handleX, y + height / 2, handleWidth / 2, height - height / 2, 0, 
				0, defaultHeight - (height - height / 2), handleWidth / 2, height - height / 2, handleWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, handleSprite, 
				handleX + handleWidth / 2, y, handleWidth / 2, height / 2, 0, 
				handleWidth / 2, 0, handleWidth - handleWidth / 2, height / 2, handleWidth, defaultHeight, BlitFloat.NO_TINT);
		BlitFloat.blit(guiGraphics.pose(), minecraft, handleSprite, 
				handleX + handleWidth / 2, y + height / 2, handleWidth / 2, height - height / 2, 0, 
				handleWidth / 2, defaultHeight - (height - height / 2), handleWidth - handleWidth / 2, height - height / 2, handleWidth, defaultHeight, BlitFloat.NO_TINT);

		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		int color = this.active ? 0xFFFFFF : 0xA0A0A0;
		this.renderScrollingString(guiGraphics, minecraft.font, 2, ARGB.color(this.alpha, color));
	}

}
