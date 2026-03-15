package com.github.standobyte.jojo.client.ui.screen_widgets.utils;

import net.minecraft.client.gui.components.AbstractWidget;

public interface ExtendedWidget {
	WidgetExtension getWidgetExtension();
	AbstractWidget thisAsWidget();
}
