package com.github.standobyte.jojo.client.ui.screen_widgets.utils;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetExtension {
	private final AbstractWidget originWidget;

	private int yStarting;

	public WidgetExtension(AbstractWidget originWidget) {
		this.originWidget = originWidget;
		this.yStarting = originWidget.getY();
	}


	public void setY(int y) {
		originWidget.setY(y);
		this.yStarting = y;
	}

	public int getYStarting() {
		return yStarting;
	}

	public void updateY(int scrollY) {
		originWidget.setY(this.yStarting + scrollY);
	}
}
