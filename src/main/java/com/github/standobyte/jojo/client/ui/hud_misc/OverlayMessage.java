package com.github.standobyte.jojo.client.ui.hud_misc;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class OverlayMessage {
	@Nullable protected Component overlayMessageString;
	protected int overlayMessageTime;
	protected boolean animateOverlayMessageColor;

	public void renderOverlayMessage(GuiGraphics guiGraphics, int yShift) {
		if (overlayMessageString == null) return;
		int alpha = getAlphaChannel(); if (alpha <= 0) return;

		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(guiGraphics.guiWidth() / 2, guiGraphics.guiHeight() - yShift, 0);
		int rgb = animateOverlayMessageColor ? getAnimatedRGB() : 0xFFFFFF;
		int color = FastColor.ARGB32.color(alpha, rgb);

		int width = font.width(overlayMessageString);
		guiGraphics.drawStringWithBackdrop(font, overlayMessageString, -width / 2, -4, width, color);
		guiGraphics.pose().popPose();
	}
	
	public void setOverlayMessage(Component component, boolean animateColor) {
		this.overlayMessageString = component;
		this.overlayMessageTime = 60;
		this.animateOverlayMessageColor = animateColor;
	}

	public void tick() {
		if (this.overlayMessageTime > 0) {
			this.overlayMessageTime--;
		}
	}

	public int getAlphaChannel() {
		if (overlayMessageTime <= 0) return 0;
		float timeLeft = getTimeLeft();
		int alpha = (int)(timeLeft * 255.0F / 20.0F);
		if (alpha > 255) {
			alpha = 255;
		}
		return alpha > 8 ? alpha : 0;
	}

	public int getAnimatedRGB() {
		float timeLeft = getTimeLeft();
		return Mth.hsvToRgb(timeLeft / 50.0F, 0.7F, 0.6F);
	}

	protected float getTimeLeft() {
		float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
		return (float)overlayMessageTime - partialTick;
	}

}
