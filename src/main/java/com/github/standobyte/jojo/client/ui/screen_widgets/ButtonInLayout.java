package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.function.Consumer;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;

public class ButtonInLayout implements Layout {
	public AbstractWidget button;
	public Consumer<AbstractWidget> arrange;
	
	public ButtonInLayout(AbstractWidget button, Consumer<AbstractWidget> arrange) {
		this.button = button;
		this.arrange = arrange;
	}

	@Override
	public void setX(int x) {
//		button.setX(x);
	}

	@Override
	public void setY(int y) {
//		button.setY(y);
	}

	@Override
	public int getX() {
		return button.getX();
	}

	@Override
	public int getY() {
		return button.getY();
	}

	@Override
	public int getWidth() {
		return button.getWidth();
	}

	@Override
	public int getHeight() {
		return button.getHeight();
	}

	@Override
	public void visitChildren(Consumer<LayoutElement> visitor) {
		visitor.accept(button);
	}
	
	@Override
	public void arrangeElements() {
		arrange.accept(button);
	}

}
