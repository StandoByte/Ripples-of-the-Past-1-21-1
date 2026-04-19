package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

@Deprecated
public class IconButton extends ButtonWithImage {

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.icon(icon), 
				pOnPress);
	}

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.icon(icon), 
				pOnPress, pOnTooltip);
	}

	public IconButton(int pX, int pY, int pWidth, int pHeight, 
			GuiIcon icon, 
			Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.icon(icon), 
				pOnPress, pOnTooltip, pMessage);
	}

}
