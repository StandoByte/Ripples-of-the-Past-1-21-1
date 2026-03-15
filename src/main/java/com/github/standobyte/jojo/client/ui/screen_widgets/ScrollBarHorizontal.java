package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.OnMouseRelease;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;

public abstract class ScrollBarHorizontal extends AbstractWidget implements OnMouseRelease {
	protected final int xInitial; 
	protected final boolean loopsBack;
	protected boolean isMovingScrollBox = false;
	protected float scrollBoxXClicked;

	public ScrollBarHorizontal(int x, int y, int width, int height, boolean loopsBack) {
		super(x, y, width, height, CommonComponents.EMPTY);
		this.xInitial = x;
		this.loopsBack = loopsBack;
	}

	protected abstract int getScrollBoxLength();
	protected abstract float getXScroll();
	protected abstract void setXScroll(float xScroll);
	protected abstract void renderScrollBox(GuiGraphics guiGraphics, int scrollBoxLength, float xScroll);
	
	public int getMaxXScroll() {
		return width - getScrollBoxLength();
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		float xScroll = getXScroll();
		int scrollBoxLength = getScrollBoxLength();
		int x = getX();
		int y = getY();
		scrollBoxXClicked = (float) mouseX - xScroll - x;
		isMovingScrollBox = 
				mouseX >= x + xScroll && mouseX <= x + xScroll + scrollBoxLength && 
				mouseY >= y && mouseY <= y + height;
				if (!isMovingScrollBox) {
					float xScrollF = ((float) mouseX - x - scrollBoxLength / 2);
					setClampXScroll(xScrollF);
					scrollBoxXClicked = (float) mouseX - getXScroll() - x;
					isMovingScrollBox = true;
				}
	}

	@Override
	public boolean onMouseReleaseAnywhere(int mouseButton) {
		if (isMovingScrollBox && isValidClickButton(mouseButton)) {
			isMovingScrollBox = false;
			return true;
		}
		return false;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int scrollBoxLength = getScrollBoxLength();
		if (isMovingScrollBox) {
			setClampXScroll(mouseX - scrollBoxXClicked - getX());
		}
		if (scrollBoxLength > 0) {
			renderScrollBox(guiGraphics, scrollBoxLength, getXScroll());
		}
	}

	public void setClampXScroll(float xScroll) {
		if (loopsBack) {
			if (xScroll < 0) {
				xScroll += width * (-(int) (xScroll % width) + 1);
			}
			setXScroll(xScroll % width);
		}
		else {
			setXScroll(Mth.clamp(xScroll, 0, width - getScrollBoxLength()));
		}
	}

	public void updateScroll() {
		if (!loopsBack) {
			if (getXScroll() < 0) setXScroll(0);
			else if (getXScroll() > width - getScrollBoxLength()) setXScroll(width - getScrollBoxLength());
		}
	}

	@Override
	public void playDownSound(SoundManager soundHandler) {}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}
}
