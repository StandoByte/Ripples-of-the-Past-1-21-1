package com.github.standobyte.jojo.client.ui;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class DrawHotbar {
	public static final ResourceLocation HOTBARS_TEX = JojoMod.resLoc("textures/gui/overlay_hotbar.png");
	public static final ResourceLocation HOTBARS_TEX_GREEN = JojoMod.resLoc("textures/gui/overlay_hotbar_green.png");
	public static final int TEX_SIZE = 512;
	public static final GuiIcon[] HOTBARS = new GuiIcon[] {
			new GuiIcon(HOTBARS_TEX, 390,  50,  50, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 370, 100,  70, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 350, 150,  90, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 330, 200, 110, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 310, 250, 130, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 290, 300, 150, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 270, 350, 170, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 250, 400, 190, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX, 230, 450, 210, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 450, 230, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 400, 250, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 350, 270, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 300, 290, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 250, 310, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 200, 340, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 150, 350, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0, 100, 370, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0,  50, 390, 50, TEX_SIZE, TEX_SIZE),
			new GuiIcon(HOTBARS_TEX,   0,   0, 410, 50, TEX_SIZE, TEX_SIZE)
	};
	
	public static final GuiIcon HOTBAR_SELECTION = new GuiIcon(HOTBARS_TEX, 450, 10, 52, 52, TEX_SIZE, TEX_SIZE);
	
	
	public static void drawHotbarSlot(int length, int slot, boolean isGreen, 
			PoseStack poseStack, float x, float y, int color) {
		if (length < 0) throw new IllegalArgumentException();
		
		ResourceLocation sheet = isGreen ? HOTBARS_TEX_GREEN : HOTBARS_TEX;
		GuiIcon hotbarSprite = HOTBARS[Math.min(length, HOTBARS.length) - 1];
		
		float u = hotbarSprite.minU * TEX_SIZE;
		float v = hotbarSprite.minV * TEX_SIZE;
		float width = 20;
		float height = 50;
		y -= 14;
		if (slot == 0) {
			x -= 14;
			width += 15;
		}
		else {
			x += 1;
			u += 15 + slot * 20;
		}
		if (slot == length - 1) {
			width += 15;
		}
		
		BlitFloat.blit(poseStack, Minecraft.getInstance(), sheet, 
				x, y, width, height, 0, 
				u, v, width, height, TEX_SIZE, TEX_SIZE, 
				color);
	}
	
	// TODO draw this only after the entire hotbar
	public static void drawHotbarSelection(boolean isGreen, 
			PoseStack poseStack, float x, float y, int color) {
		ResourceLocation sheet = isGreen ? HOTBARS_TEX_GREEN : HOTBARS_TEX;
		float u = 450;
		float v = 10;
		float width = 52;
		float height = 52;
		x -= 15;
		y -= 15;
		BlitFloat.blit(poseStack, Minecraft.getInstance(), sheet, 
				x, y, width, height, 0, 
				u, v, width, height, TEX_SIZE, TEX_SIZE, 
				color);
	}

}
