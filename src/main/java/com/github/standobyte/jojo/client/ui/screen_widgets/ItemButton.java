package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Deprecated
public class ItemButton extends ButtonWithImage {

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.item(item), 
				pOnPress);
	}

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress, Tooltip pOnTooltip) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.item(item), 
				pOnPress, pOnTooltip);
	}

	public ItemButton(int pX, int pY, int pWidth, int pHeight, 
			ItemStack item, 
			Button.OnPress pOnPress, Tooltip pOnTooltip, Component pMessage) {
		super(pX, pY, pWidth, pHeight, 
				ButtonDecoration.item(item), 
				pOnPress, pOnTooltip, pMessage);
	}

}
