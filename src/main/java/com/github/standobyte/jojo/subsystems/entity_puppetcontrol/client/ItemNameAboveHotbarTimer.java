package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ItemNameAboveHotbarTimer {
	public int toolHighlightTimer;
	public ItemStack lastToolHighlight = ItemStack.EMPTY;

	public void renderSelectedItemName(GuiGraphics guiGraphics, Minecraft mc) {
		renderSelectedItemName(guiGraphics, 
				guiGraphics.guiWidth() / 2, 
				guiGraphics.guiHeight() - MathUtil.max(mc.gui.leftHeight, mc.gui.rightHeight, 59), 
				mc);
	}

	public void renderSelectedItemName(GuiGraphics guiGraphics, int xCenter, int y, Minecraft mc) {
		if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
			MutableComponent name = Component.empty()
					.append(this.lastToolHighlight.getHoverName())
					.withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
			if (this.lastToolHighlight.has(DataComponents.CUSTOM_NAME)) {
				name.withStyle(ChatFormatting.ITALIC);
			}

			Component highlightTip = this.lastToolHighlight.getHighlightTip(name);
			int width = mc.font.width(highlightTip);
			int x = xCenter - width / 2;

			int alpha = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
			if (alpha > 255) {
				alpha = 255;
			}

			if (alpha > 0) {
				Font font = IClientItemExtensions.of(lastToolHighlight).getFont(lastToolHighlight, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
				if (font == null) {
					guiGraphics.drawStringWithBackdrop(mc.font, highlightTip, x, y, width, FastColor.ARGB32.color(alpha, -1));
				} else {
					x = (guiGraphics.guiWidth() - font.width(highlightTip)) / 2;
					guiGraphics.drawStringWithBackdrop(font, highlightTip, x, y, width, FastColor.ARGB32.color(alpha, -1));
				}
			}
		}
	}

	public void tick(ItemStack selectedItem, Minecraft mc) {
		if (selectedItem.isEmpty()) {
			this.toolHighlightTimer = 0;
		} else if (this.lastToolHighlight.isEmpty()
				|| !selectedItem.is(this.lastToolHighlight.getItem())
				|| (!selectedItem.getHoverName().equals(this.lastToolHighlight.getHoverName()) || !selectedItem.getHighlightTip(selectedItem.getHoverName()).equals(this.lastToolHighlight.getHighlightTip(this.lastToolHighlight.getHoverName())))) {
			this.toolHighlightTimer = (int)(40.0 * mc.options.notificationDisplayTime().get());
		} else if (this.toolHighlightTimer > 0) {
			this.toolHighlightTimer--;
		}

		this.lastToolHighlight = selectedItem;
	}
}
