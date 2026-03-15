package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.OnMouseRelease;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;

public abstract class ScrollBarVertical extends AbstractWidget implements OnMouseRelease {
	protected final int yInitial;
	protected boolean isMovingScrollBox = false;
	protected int scrollBoxYClicked;

	public ScrollBarVertical(int x, int y, int width, int height) {
		super(x, y, width, height, CommonComponents.EMPTY);
		this.yInitial = y;
	}

	protected abstract int getScrollBoxHeight();
	protected abstract int getYScroll();
	protected abstract void setYScroll(int yScroll);
	protected abstract void renderScrollBox(GuiGraphics guiGraphics, int scrollBoxHeight, int yScroll);

	public int getMaxYScroll() {
		return height - getScrollBoxHeight();
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		int yScroll = getYScroll();
		int x = getX();
		int y = getY();
		scrollBoxYClicked = (int) mouseY - yScroll - y;
		isMovingScrollBox = 
				mouseX >= x && mouseX <= x + width && 
				mouseY >= y + yScroll && mouseY <= y + yScroll + getScrollBoxHeight();
		if (!isMovingScrollBox) {
			int scrollBoxHeight = getScrollBoxHeight();
			setYScroll(Mth.clamp((int) ((mouseY - y - scrollBoxHeight / 2)), 0, height - scrollBoxHeight));
			scrollBoxYClicked = (int) mouseY - getYScroll() - y;
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
		int scrollBoxHeight = getScrollBoxHeight();
		if (isMovingScrollBox) {
			setYScroll(Mth.clamp(mouseY - scrollBoxYClicked - getY(), 0, height - scrollBoxHeight));
		}
		if (scrollBoxHeight > 0) {
			renderScrollBox(guiGraphics, scrollBoxHeight, getYScroll());
		}
	}

	public void updateScroll() {
		if (getYScroll() < 0) setYScroll(0);
		else if (getYScroll() > height - getScrollBoxHeight()) setYScroll(height - getScrollBoxHeight());
	}

	@Override
	public void playDownSound(SoundManager soundHandler) {}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}
}
