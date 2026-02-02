package com.github.standobyte.jojo.client.ui.utils;

import javax.annotation.Nullable;

import com.github.standobyte.v1_21_4_stuff.GuiScissor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class Scrolling {
	public int uiHeight;
	public int contentsHeight;
	public float heightRatio;
	public float scrollSpeed = 8;
	
	public float scrollOffset;
	
	public Scrolling(int uiHeight) {
		this.uiHeight = uiHeight;
	}
	
	public Scrolling(int uiHeight, int contentsHeight) {
		this.uiHeight = uiHeight;
		setContentsHeight(contentsHeight);
	}
	
	public void setContentsHeight(int contentsHeight) {
		this.contentsHeight = contentsHeight;
		this.heightRatio = contentsHeight > uiHeight ? (float) uiHeight / (float) contentsHeight : 1;
		setScrollOffset(scrollOffset); // to clamp
	}
	
	public int getMaxScrollOffset() {
		return Math.max(contentsHeight - uiHeight, 0);
	}
	
	public boolean hasScrolling() {
		return contentsHeight > uiHeight;
	}
	
	public void scroll(double scrollDir) {
		setScrollOffset(this.scrollOffset + (float) scrollDir * scrollSpeed);
	}
	
	public void setScrollOffset(float offset) {
		this.scrollOffset = Mth.clamp(offset, -getMaxScrollOffset(), 0);
	}
	
	
	public void pushOffsetScissor(GuiGraphics guiGraphics, int y, int x0, int x1) {
		GuiScissor.enableScissor(guiGraphics, x0, y, x1, y + uiHeight);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, scrollOffset, 0);
	}
	
	public void pop(GuiGraphics guiGraphics) {
		guiGraphics.disableScissor();
		guiGraphics.pose().popPose();
	}
	
	public int calcScrollBarHeight() {
		return (int) (uiHeight * heightRatio);
	}
	
	@Nullable
	public int[] getScrollBarBounds(int barHeightOffset, int minHeight) {
		if (!hasScrolling()) {
			return null;
		}
		int barHeight = Math.max(calcScrollBarHeight() + barHeightOffset * 2, minHeight);
		int barTop = (int) (
				(float) (uiHeight - barHeight + barHeightOffset) * (
						(-scrollOffset - barHeightOffset * 2) / 
						((float) getMaxScrollOffset() - barHeightOffset * 2))
				);
		return new int[] { barTop, barTop + barHeight };
	}
	
	public void renderScrollBar(float x, float y, 
			int barHeightOffset, int minHeight, 
			GuiGraphics guiGraphics, 
			GuiIcon sprite, int usePixelsFromBottom) {
		int[] bounds = getScrollBarBounds(barHeightOffset, minHeight);
		if (bounds != null) {
			y += bounds[0];
			int barHeight = bounds[1] - bounds[0];
			
			if (usePixelsFromBottom != 69 && barHeight < sprite.height) {
				int bottomHalfHeight = usePixelsFromBottom;
				int topHalfHeight = barHeight - usePixelsFromBottom;
				float topHalfHeightV = (float) topHalfHeight / sprite.texHeight;
				float bottomHalfHeightV = (float) bottomHalfHeight / sprite.texHeight;
				
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), sprite.file, 
						x, y, sprite.width, topHalfHeight, 0, 
						sprite.minU, sprite.minV, sprite.widthU, topHalfHeightV, 1, 1, 
						BlitFloat.NO_TINT);
				
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), sprite.file, 
						x, y + topHalfHeight, sprite.width, barHeight - topHalfHeight, 0, 
						sprite.minU, sprite.minV + sprite.heightV - bottomHalfHeightV, sprite.widthU, bottomHalfHeightV, 1, 1, 
						BlitFloat.NO_TINT);
			}
			else {
				BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), sprite.file, 
						x, y, sprite.width, sprite.height, 0, 
						sprite.minU, sprite.minV, sprite.widthU, sprite.heightV, 1, 1, 
						BlitFloat.NO_TINT);
			}
		}
	}
	
	public int getYHovered(int uiPosY, int mouseY) {
		if (mouseY < uiPosY || mouseY > uiPosY + uiHeight) {
			return -1;
		}
		return (int) (mouseY - uiPosY - scrollOffset);
	}

}
