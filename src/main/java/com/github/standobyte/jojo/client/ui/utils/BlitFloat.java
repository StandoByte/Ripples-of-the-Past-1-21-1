package com.github.standobyte.jojo.client.ui.utils;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.util.functions.MathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * UI rendering stuff changed between versions, so these functions are still kinda error-prone.
 */
public class BlitFloat {
	public static final int NO_TINT = 0xFFFFFFFF;

	//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
	//			float pX, float pY, float pUOffset, float pVOffset, float pWidth, float pHeight) {
	//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
	//				pX, pX + pWidth, pY, pY + pHeight, 
	//				pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, 
	//				-1);
	//	}
	//	
	//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
	//			float pX, float pY, float pUOffset, float pVOffset, 
	//			float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
	//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
	//				pX, pX + pUWidth, pY, pY + pVHeight, 
	//				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
	//				-1);
	//	}
	//	
	//	public static void blitFloat(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation texture, 
	//			float pX, float pY, float pWidth, float pHeight, 
	//			float pUOffset, float pVOffset, float pUWidth, float pVHeight, float pTextureWidth, float pTextureHeight) {
	//		innerBlitFloat(guiGraphics, mc, RenderType::guiTextured, texture, 
	//				pX, pX + pWidth, pY, pY + pHeight, 
	//				pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, 
	//				-1);
	//	}
	//	
	//	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, 
	//			Function<ResourceLocation, RenderType> renderTypeGetter, ResourceLocation texture, 
	//			float pX1, float pX2, float pY1, float pY2, 
	//			float pUWidth, float pVHeight, float pUOffset, float pVOffset, float pTextureWidth, float pTextureHeight, 
	//			int color) {
	//		innerBlitFloat(guiGraphics, mc, renderTypeGetter.apply(texture), 
	//				pX1, pX2, pY1, pY2, 
	//				(pUOffset + 0.0F) / pTextureWidth, 
	//				(pUOffset + pUWidth) / pTextureWidth, 
	//				(pVOffset + 0.0F) / pTextureHeight, 
	//				(pVOffset + pVHeight) / pTextureHeight, 
	//				color);
	//	}
	//	
	//	public static void innerBlitFloat(GuiGraphics guiGraphics, Minecraft mc, RenderType renderType, 
	//			float x1, float x2, float y1, float y2, 
	//			float minU, float maxU, float minV, float maxV, 
	//			int color) {
	//		Matrix4f matrix4f = guiGraphics.pose().last().pose();
	//		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
	//		VertexConsumer vertexconsumer = bufferSource.getBuffer(renderType);
	//		vertexconsumer.addVertex(matrix4f, x1, y1, 0.0F).setUv(minU, minV).setColor(color);
	//		vertexconsumer.addVertex(matrix4f, x1, y2, 0.0F).setUv(minU, maxV).setColor(color);
	//		vertexconsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
	//		vertexconsumer.addVertex(matrix4f, x2, y1, 0.0F).setUv(maxU, minV).setColor(color);
	//	}



	public static void blit(PoseStack poseStack, Minecraft mc, TextureAtlasSprite sprite,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			int tint) {
		float u0 = sprite.getU0();
		float v0 = sprite.getV0();
		float uWidth = sprite.getU1() - u0;
		float vHeight = sprite.getV1() - v0;
		blit(poseStack, mc, 
				sprite.atlasLocation(),
				x0, y0, xWidth, yHeight, blitOffset,
				u0, v0, uWidth, vHeight, 1, 1, tint);
	}

	public static void blit(PoseStack poseStack, Minecraft mc, ResourceLocation sprite,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			int tint) {
		blit(poseStack, mc, 
				sprite,
				x0, y0, xWidth, yHeight, blitOffset,
				0, 0, xWidth, yHeight, xWidth, yHeight, 
				tint);
	}

	public static void blit(PoseStack poseStack, Minecraft mc, ResourceLocation texture,
			float x0, float y0, float xWidth, float yHeight, float blitOffset,
			float u0, float v0, float uWidth, float vHeight, float textureWidth, float textureHeight, 
			int tint) {
		RenderSystem.setShaderTexture(0, texture);
		Matrix4f matrix4f = poseStack.last().pose();
		BufferBuilder bufferbuilder = setupBuffer(tint);

		float x1 = x0 + xWidth;
		float y1 = y0 + yHeight;
		float u1 = u0 + uWidth;
		float v1 = v0 + vHeight;
		u0 /= textureWidth;
		u1 /= textureWidth;
		v0 /= textureHeight;
		v1 /= textureHeight;
		bufferbuilder.addVertex(matrix4f, x0, y0, blitOffset).setUv(u0, v0).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x0, y1, blitOffset).setUv(u0, v1).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(u1, v1).setColor(tint);
		bufferbuilder.addVertex(matrix4f, x1, y0, blitOffset).setUv(u1, v0).setColor(tint);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
	}

	protected static BufferBuilder setupBuffer(int tint) {
		if ((tint & NO_TINT) != NO_TINT) {
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		}
		else {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		}
	}


	public static void blitRadial(PoseStack poseStack, Minecraft mc, ResourceLocation texture,
			float minX, float minY, float xWidth, float yHeight, float blitOffset,
			float angle0, float fill, int tint) {
		blitRadial(poseStack, mc, texture, 
				minX, minY, xWidth, yHeight, blitOffset, 
				0, 0, xWidth, yHeight, xWidth, yHeight, 
				angle0, fill, tint);
	}

	public static void blitRadial(PoseStack poseStack, Minecraft mc, ResourceLocation texture,
			float minX, float minY, float xWidth, float yHeight, float blitOffset,
			float minU, float minV, float uWidth, float vHeight, float textureWidth, float textureHeight, 
			float angle0, float fill, int tint) {
		if (fill == 0) return;
		if (fill <= -1 || fill >= 1) {
			BlitFloat.blit(poseStack, mc, texture,
					minX, minY, xWidth, yHeight, blitOffset,
					minU, minV, uWidth, vHeight, textureWidth, textureHeight, 
					tint);
			return;
		}

		RenderSystem.setShaderTexture(0, texture);
		Matrix4f matrix4f = poseStack.last().pose();

		angle0 = MathUtil.wrapRadians(angle0);
		float angle1 = angle0 + PI * 2 * fill;
		if (fill < 0) {
			float swap = angle1;
			angle1 = angle0;
			angle0 = swap;

			float angle0Wr = MathUtil.wrapRadians(angle0);
			angle1 += (angle0Wr - angle0);
			angle0 += (angle0Wr - angle0);
		}

		float maxX = minX + xWidth;
		float maxY = minY + yHeight;
		float maxU = minU + uWidth;
		float maxV = minV + vHeight;
		minU /= textureWidth;
		maxU /= textureWidth;
		minV /= textureHeight;
		maxV /= textureHeight;
		float halfWidth = xWidth / 2;
		float halfHeight = yHeight / 2;

		float x0 = (minX + maxX) / 2;
		float y0 = (minY + maxY) / 2;
		float x1;
		float y1;
		float x2;
		float y2;
		float x3;
		float y3;
		float angleV1;
		float angleV3;
		float cosV1;
		float sinV1;
		float cosV3;
		float sinV3;
		float scaleV1;
		float scaleV3;

		for (Quadrant quadrant : Quadrant.values()) {
			if (quadrant.ordinal() == 2 && angle1 > PI) {
				angle0 -= PI * 2;
				angle1 -= PI * 2;
			}

			if (angle1 > quadrant.minAngle && angle0 <= quadrant.maxAngle) {
				angleV1 = Math.max(angle0, quadrant.minAngle) - quadrant.minAngle;
				angleV3 = Math.min(angle1, quadrant.maxAngle) - quadrant.minAngle;
				cosV1 = Mth.cos(angleV1);
				sinV1 = Mth.sin(angleV1);
				cosV3 = Mth.cos(angleV3);
				sinV3 = Mth.sin(angleV3);

				switch (quadrant) {
				case LOWER_RIGHT:
					x1 = cosV1;
					y1 = sinV1;
					x3 = cosV3;
					y3 = sinV3;
					break;
				case UPPER_RIGHT:
					x1 =  sinV1;
					y1 = -cosV1;
					x3 =  sinV3;
					y3 = -cosV3;
					break;
				case UPPER_LEFT:
					x1 = -cosV1;
					y1 = -sinV1;
					x3 = -cosV3;
					y3 = -sinV3;
					break;
				case LOWER_LEFT:
					x1 = -sinV1;
					y1 =  cosV1;
					x3 = -sinV3;
					y3 =  cosV3;
					break;
				default:
					throw new AssertionError();
				}

				scaleV1 = cosV1 > sinV1 ? halfWidth / cosV1 : halfHeight / sinV1;
				scaleV3 = cosV3 > sinV3 ? halfWidth / cosV3 : halfHeight / sinV3;
				x1 *= scaleV1;
				y1 *= scaleV1;
				x3 *= scaleV3;
				y3 *= scaleV3;
				x1 += x0;
				y1 += y0;
				x3 += x0;
				y3 += y0;

				switch (quadrant) {
				case LOWER_RIGHT:
					x2 = Math.min(x1, maxX);
					y2 = Math.min(y3, maxY);
					break;
				case UPPER_RIGHT:
					x2 = Math.min(x3, maxX);
					y2 = Math.max(y1, minY);
					break;
				case UPPER_LEFT:
					x2 = Math.max(x1, minX);
					y2 = Math.max(y3, minY);
					break;
				case LOWER_LEFT:
					x2 = Math.max(x3, minX);
					y2 = Math.min(y1, maxY);
					break;
				default:
					throw new AssertionError();
				}

				BufferBuilder bufferbuilder = setupBuffer(tint);
				bufferbuilder.addVertex(matrix4f, x3, y3, blitOffset).setUv(
						lerpUV(x3, minX, maxX, minU, maxU), 
						lerpUV(y3, minY, maxY, minV, maxV))
				.setColor(tint);
				bufferbuilder.addVertex(matrix4f, x2, y2, blitOffset).setUv(
						lerpUV(x2, minX, maxX, minU, maxU), 
						lerpUV(y2, minY, maxY, minV, maxV))
				.setColor(tint);
				bufferbuilder.addVertex(matrix4f, x1, y1, blitOffset).setUv(
						lerpUV(x1, minX, maxX, minU, maxU), 
						lerpUV(y1, minY, maxY, minV, maxV))
				.setColor(tint);
				bufferbuilder.addVertex(matrix4f, x0, y0, blitOffset).setUv(
						lerpUV(x0, minX, maxX, minU, maxU), 
						lerpUV(y0, minY, maxY, minV, maxV))
				.setColor(tint);
				BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
			}
		}
	}

	private static final float PI = (float) Math.PI;
	private static enum Quadrant {
		LOWER_RIGHT( PI / 2,  PI),
		UPPER_RIGHT( 0,       PI / 2),
		UPPER_LEFT( -PI / 2,  0),
		LOWER_LEFT( -PI,     -PI / 2);

		private final float minAngle;
		private final float maxAngle;

		private Quadrant(float minAngle, float maxAngle) {
			this.minAngle = minAngle;
			this.maxAngle = maxAngle;
		}
	}

	private static float lerpUV(float coord, float coord0, float coord1, float minUV, float maxUV) {
		return (float) Mth.lerp(Mth.inverseLerp(coord, coord0, coord1), minUV, maxUV);
	}
}
