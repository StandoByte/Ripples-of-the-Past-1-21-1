package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ExtendedWidget;
import com.github.standobyte.jojo.client.ui.screen_widgets.utils.WidgetExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ToggleBox extends AbstractWidget implements ExtendedWidget {
	protected final WidgetExtension extension;

	@Nullable protected final Supplier<Boolean> stateGet;
	@Nullable protected final Consumer<Boolean> stateSet;
	protected boolean stateDefault;

	public ToggleBox(int x, int y, int width, int height, Component name, 
			Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, @Nullable Tooltip tooltip) {
		super(x, y, width, height, name);
		this.extension = new WidgetExtension(this);
		this.stateGet = stateGet;
		this.stateSet = stateSet;
		this.stateDefault = getState();
		setTooltip(tooltip);
	}

	public ToggleBox(int x, int y, int width, int height, Component name, 
			boolean startingState) {
		this(x, y, width, height, name, null, null, null);
		this.stateDefault = startingState;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		toggle();
	}

	public void toggle() {
		if (stateSet != null) {
			stateSet.accept(!getState());
		}
		stateDefault = !stateDefault;
	}

	public boolean getState() {
		return stateGet != null ? stateGet.get() : stateDefault;
	}

	public void updateFromState() {
		if (stateSet != null) {
			stateSet.accept(getState());
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), 
				x + width / 2, y + (height - 8) / 2, getFGColor() | Mth.ceil(alpha * 255.0F) << 24);
	}



	@Override
	public WidgetExtension getWidgetExtension() {
		return extension;
	}

	@Override
	public AbstractWidget thisAsWidget() {
		return this;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}
}
