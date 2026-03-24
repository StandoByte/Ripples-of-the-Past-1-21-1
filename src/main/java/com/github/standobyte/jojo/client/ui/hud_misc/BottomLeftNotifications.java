package com.github.standobyte.jojo.client.ui.hud_misc;

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.standobyte.jojo.client.ui.utils.ElementTransparency;
import com.github.standobyte.jojo.client.ui.utils.FontUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// appear at the bottom and get pushed to the top
public class BottomLeftNotifications {
	public static Deque<NotificationLine> _lines = new ArrayDeque<>();

	public static void add(Component text) {
		NotificationLine line = new NotificationLine(text, new ElementTransparency(100, 25));
		_lines.add(line);
	}
	
	public static void _tick() {
		var iter = _lines.iterator();
		while (iter.hasNext()) {
			NotificationLine line = iter.next();
			if (!line.fadeOut().shouldRender()) {
				iter.remove();
			}
		}
	}
	
	static final int LINE_HEIGHT = 6;
	public static void render(GuiGraphics guiGraphics, float partialTick) {
		if (!_lines.isEmpty()) {
			PoseStack poseStack = guiGraphics.pose();
			Font font = Minecraft.getInstance().font;
			poseStack.pushPose();
			poseStack.scale(0.5f, 0.5f, 1);
			int x = 10;
			int y = guiGraphics.guiHeight() - 5 - _lines.size() * LINE_HEIGHT;
			for (NotificationLine line : _lines) {
				float alpha = line.fadeOut.getAlpha(partialTick);
				if (alpha > 0) {
					FontUtil.drawWithBackdrop(guiGraphics, font, line.text, x * 2, y * 2, ARGB.white(alpha));
				}
				y += LINE_HEIGHT;
			}
			poseStack.popPose();
		}
	}
	
	public static record NotificationLine(Component text, ElementTransparency fadeOut) {}
}
