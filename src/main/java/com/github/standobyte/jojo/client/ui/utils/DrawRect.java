package com.github.standobyte.jojo.client.ui.utils;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

public class DrawRect {

	public static void fillSingleRect(GuiGraphics guiGraphics, float x, float y, float width, float height, int color) {
		RenderType renderType = RenderType.guiOverlay();

		Matrix4f matrix4f = guiGraphics.pose().last().pose();

		VertexConsumer vertexСonsumer = guiGraphics.bufferSource().getBuffer(renderType);
		vertexСonsumer.addVertex(matrix4f, x, y, 0).setColor(color);
		vertexСonsumer.addVertex(matrix4f, x, y + height, 0).setColor(color);
		vertexСonsumer.addVertex(matrix4f, x + width, y + height, 0).setColor(color);
		vertexСonsumer.addVertex(matrix4f, x + width, y, 0).setColor(color);
		//guiGraphics.flushIfUnmanaged();
	}
}
