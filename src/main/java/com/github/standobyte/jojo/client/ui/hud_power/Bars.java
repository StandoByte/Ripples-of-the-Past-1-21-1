package com.github.standobyte.jojo.client.ui.hud_power;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class Bars {
	public static final int HORIZONTAL_LENGTH = 202;
	public static final int HORIZONTAL_WIDTH = 8;
	public static final int HORIZONTAL_LENGTH_MINI = 152;
	public static final int HORIZONTAL_WIDTH_MINI = 5;
	public static final int VERTICAL_HEIGHT = 102;
	public static final int VERTICAL_WIDTH = 8;
	public static final int VERTICAL_HEIGHT_MINI = 77;
	public static final int VERTICAL_WIDTH_MINI = 5;
	
	public static final ResourceLocation BAR_HORIZONTAL_EMPTY = JojoMod.resLoc("textures/hud/bars/bar_horizontal_empty.png");
	public static final ResourceLocation BAR_HORIZONTAL_SCALE = JojoMod.resLoc("textures/hud/bars/bar_horizontal_scale.png");
	public static final ResourceLocation BAR_HORIZONTAL_MINI_EMPTY = JojoMod.resLoc("textures/hud/bars/bar_horizontal_mini_empty.png");
	public static final ResourceLocation BAR_HORIZONTAL_MINI_SCALE = JojoMod.resLoc("textures/hud/bars/bar_horizontal_mini_scale.png");
	public static final ResourceLocation BAR_VERTICAL_EMPTY = JojoMod.resLoc("textures/hud/bars/bar_vertical_empty.png");
	public static final ResourceLocation BAR_VERTICAL_SCALE = JojoMod.resLoc("textures/hud/bars/bar_vertical_scale.png");
	public static final ResourceLocation BAR_VERTICAL_MINI_EMPTY = JojoMod.resLoc("textures/hud/bars/bar_vertical_mini_empty.png");
	public static final ResourceLocation BAR_VERTICAL_MINI_SCALE = JojoMod.resLoc("textures/hud/bars/bar_vertical_mini_scale.png");
	
	public static final ResourceLocation BAR_HORIZONTAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_fill.png");
	public static final ResourceLocation BAR_HORIZONTAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_mini_fill.png");
	public static final ResourceLocation BAR_VERTICAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_fill.png");
	public static final ResourceLocation BAR_VERTICAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_mini_fill.png");
	
	public enum BarSize { REGULAR, MINI }

	
	public static void renderHorizontalBar(PoseStack poseStack, float x, float y, 
			float barFill, int fillTint, float alpha) {
		renderHorizontalBar(poseStack, x, y, barFill, BAR_HORIZONTAL_FILL, fillTint, alpha);
	}
	
	public static void renderHorizontalBar(PoseStack poseStack, float x, float y, float barFill, 
			ResourceLocation barFillSprite, int fillTint, float alpha) {
		_renderHorizontalBar(poseStack, x, y, barFill, 
				HORIZONTAL_LENGTH, HORIZONTAL_WIDTH, BAR_HORIZONTAL_EMPTY, BAR_HORIZONTAL_SCALE, 
				barFillSprite, fillTint, alpha);
	}
	
	public static void renderHorizontalBarMini(PoseStack poseStack, float x, float y, 
			float barFill, int fillTint, float alpha) {
		renderHorizontalBarMini(poseStack, x, y, barFill, BAR_HORIZONTAL_MINI_FILL, fillTint, alpha);
	}
	
	public static void renderHorizontalBarMini(PoseStack poseStack, float x, float y, float barFill, 
			ResourceLocation barFillSprite, int fillTint, float alpha) {
		_renderHorizontalBar(poseStack, x, y, barFill, 
				HORIZONTAL_LENGTH_MINI, HORIZONTAL_WIDTH_MINI, BAR_HORIZONTAL_MINI_EMPTY, BAR_HORIZONTAL_MINI_SCALE, 
				barFillSprite, fillTint, alpha);
	}
	
	public static void _renderHorizontalBar(PoseStack poseStack, float x, float y, float barFill, 
			float length, float width, ResourceLocation barEmpty, ResourceLocation barScale, 
			ResourceLocation barFillSprite, int fillTint, float alpha) {
		int colorMain = ARGB.white(alpha);
		int colorFill = ARGB.color(alpha, fillTint);
		Minecraft mc = Minecraft.getInstance();
		BlitFloat.blit(poseStack, mc, barEmpty, 
				x, y, length, width, 0, 
				colorMain);
		float fillULength = (length - 2) * barFill + 1;
		BlitFloat.blit(poseStack, mc, barFillSprite, 
				x, y, fillULength, width, 0, 
				0, 0, fillULength, width, length, width, 
				colorFill);
		BlitFloat.blit(poseStack, mc, barScale, 
				x, y, length, width, 0, 
				colorMain);
	}
	
	
	public static void renderVerticalBar(PoseStack poseStack, float x, float y, float barFill, int fillTint) {
		renderVerticalBar(poseStack, x, y, barFill, BAR_VERTICAL_FILL, fillTint);
	}
	
	public static void renderVerticalBar(PoseStack poseStack, float x, float y, float barFill, ResourceLocation barFillSprite, int fillTint) {
		_renderVerticalBar(poseStack, x, y, barFill, 
				VERTICAL_HEIGHT, VERTICAL_WIDTH, BAR_VERTICAL_EMPTY, BAR_VERTICAL_SCALE, 
				barFillSprite, fillTint);
	}
	
	public static void renderVerticalBarMini(PoseStack poseStack, float x, float y, float barFill, int fillTint) {
		renderVerticalBarMini(poseStack, x, y, barFill, BAR_VERTICAL_MINI_FILL, fillTint);
	}
	
	public static void renderVerticalBarMini(PoseStack poseStack, float x, float y, float barFill, ResourceLocation barFillSprite, int fillTint) {
		_renderVerticalBar(poseStack, x, y, barFill, 
				VERTICAL_HEIGHT_MINI, VERTICAL_WIDTH_MINI, BAR_VERTICAL_MINI_EMPTY, BAR_VERTICAL_MINI_SCALE, 
				barFillSprite, fillTint);
	}
	
	public static void _renderVerticalBar(PoseStack poseStack, float x, float y, float barFill, 
			float height, float width, ResourceLocation barEmpty, ResourceLocation barScale, 
			ResourceLocation barFillSprite, int fillTint) {
		Minecraft mc = Minecraft.getInstance();
		BlitFloat.blit(poseStack, mc, barEmpty, 
				x, y, width, height, 0, 
				BlitFloat.NO_TINT);
		float fillVHeight = (height - 2) * barFill + 1;
		BlitFloat.blit(poseStack, mc, barFillSprite, 
				x, y, width, fillVHeight, 0, 
				0, height - fillVHeight, width, fillVHeight, width, height, 
				fillTint);
		BlitFloat.blit(poseStack, mc, barScale, 
				x, y, width, height, 0, 
				BlitFloat.NO_TINT);
	}
}
