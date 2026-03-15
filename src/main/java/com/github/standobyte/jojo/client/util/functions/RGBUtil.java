package com.github.standobyte.jojo.client.util.functions;

import net.minecraft.util.FastColor.ARGB32;

public class RGBUtil {
	
	public static float[] argb(int color) {
		return new float[] {
				ARGB32.alpha(color) / 255F, 
				ARGB32.red(color) / 255F, 
				ARGB32.green(color) / 255F, 
				ARGB32.blue(color) / 255F
		};
	}
	
	public static float[] rgb(int color) {
		return new float[] {
				ARGB32.red(color) / 255F, 
				ARGB32.green(color) / 255F, 
				ARGB32.blue(color) / 255F
		};
	}
	
	public static int[] argbInt(int color) {
		return new int[] {
				ARGB32.alpha(color), 
				ARGB32.red(color), 
				ARGB32.green(color), 
				ARGB32.blue(color)
		};
	}
	
//	public static int discColor(int color) {
//		return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
//	}
	
	public static int addAlpha(int color, float alpha) {
		return color | ((int) (255F * alpha)) << 24 & -0x1000000;
	}

	public static int scaleAlpha(int argbColor, float alphaScale) {
		return ARGB32.color(
				Math.clamp(((int) (ARGB32.alpha(argbColor) * alphaScale)), 0, 255),
				ARGB32.red(argbColor),
				ARGB32.green(argbColor),
				ARGB32.blue(argbColor)
				);
	}

	public static float red(int packedColor) {
		return (float) (packedColor >> 16 & 0xFF) * 255f;
	}

	public static float green(int packedColor) {
		return (float) (packedColor >> 8 & 0xFF) * 255f;
	}

	public static float blue(int packedColor) {
		return (float) (packedColor & 0xFF) * 255f;
	}
}
