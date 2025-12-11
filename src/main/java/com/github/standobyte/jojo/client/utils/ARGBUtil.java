package com.github.standobyte.jojo.client.utils;

public class ARGBUtil {

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
