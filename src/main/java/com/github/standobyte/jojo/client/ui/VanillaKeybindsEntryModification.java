package com.github.standobyte.jojo.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonWithImage;
import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mixin.client.AbstractSelectionListAccessor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class VanillaKeybindsEntryModification {

	@SuppressWarnings("unchecked")
	public static boolean replaceEntry(KeyBindsList list, KeyBindsList.KeyEntry entry) {
		KeyMapping key = entry.key;
		String keyName = key.getName();

		boolean cancelOriginal = false;
		boolean refreshEntry = false;

		ConfigBool holdOrToggle = VanillaKeybinds.HOLD_OR_TOGGLE.get(keyName);
		if (holdOrToggle != null) {
			KeyEntryWithHoldToggle holdToggleButton = new KeyEntryWithHoldToggle(list, holdOrToggle);
			KeyEntryWithNameShift changeKeyButtonWithNameInCenter = new KeyEntryWithNameShift(list, key, key.getDisplayName());
			@SuppressWarnings("rawtypes")
			AbstractSelectionListAccessor _list = (AbstractSelectionListAccessor) list;
			_list.invokeAddEntry(holdToggleButton);
			_list.invokeAddEntry(changeKeyButtonWithNameInCenter);
			entry = changeKeyButtonWithNameInCenter;
			cancelOriginal = true;
		}

		if (key == InputHandler.getInstance().vanillaKeybinds.switchSpecial) {
			GuiIcon icon = new GuiIcon(ConfigGuiHelper.toIconPath("ability_selection_wheel"), 16, 16);
			ConfigBool abilitySelectionWheel = JojoMod.config.getClient().abilitySelectionWheel;

			Component name = ConfigGuiHelper.prependIcon(
					ConfigGuiHelper.makeName(abilitySelectionWheel, JojoMod.MOD_ID, ModConfigType.CLIENT), icon);

			MultiLineScreenTooltip tooltip = new MultiLineScreenTooltip(
					abilitySelectionWheel.makeButtonMessage(name, abilitySelectionWheel.get()).withStyle(ChatFormatting.BOLD),
					ConfigGuiHelper.makeTooltipBody(abilitySelectionWheel, JojoMod.MOD_ID, ModConfigType.CLIENT));

			Button abilityWheel20x20Button = new ButtonWithImage(0, 0, 20, 20, 
					ButtonDecoration.icon(icon), 
					button -> {
						abilitySelectionWheel.set(!abilitySelectionWheel.getAsBoolean());
						tooltip.setTitle(abilitySelectionWheel.makeButtonMessage(name, abilitySelectionWheel.get()).withStyle(ChatFormatting.BOLD));
						JojoMod.config.saveClient();
					}, tooltip) {

				@Override
				public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

					int x = getX() - 10;
					int y = getBottom() - 14;
					GuiIcon icon = abilitySelectionWheel.getAsBoolean() ? CHECKMARK : CROSS;
					icon.render(guiGraphics.pose(), x, y);
				}
			};

			((KeyEntryDuck) entry).jojo_ripples$addButtonToTheLeftOfKey(abilityWheel20x20Button);
		}

		if (VanillaKeybinds.ADD_DESC_TOOLTIP.contains(keyName)) {
			((KeyEntryDuck) entry).jojo_ripples$addTooltipDescription(Component.translatable(keyName + ".desc"));
			refreshEntry = true;
		}

		if (refreshEntry) {
			((KeyEntryDuck) entry).jojo_ripples$invokeRefreshEntry();
		}
		return cancelOriginal;
	}


	public static class KeyEntryWithNameShift extends KeyBindsList.KeyEntry {
		public final Component actualName;

		public KeyEntryWithNameShift(KeyBindsList list, KeyMapping key, Component name) {
			list.super(key, CommonComponents.EMPTY);
			this.actualName = name;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index,
				int top, int left, int width, int height,
				int mouseX, int mouseY, boolean hovering, float partialTick) {
			super.render(guiGraphics, index, top, left, width, height, mouseX, mouseY, hovering, partialTick);
			int x = left;
			int y = top + height / 2 - 9 / 2;
			guiGraphics.drawString(Minecraft.getInstance().font, actualName, x, y - 10, -1);
		}

	}

	public static class KeyEntryWithHoldToggle extends KeyBindsList.Entry {
		public final KeyBindsList list;
		public ConfigBool holdOrToggleConfig;
		public Button holdToggleButton;
		public List<Button> buttonsAsList;

		public KeyEntryWithHoldToggle(KeyBindsList list, ConfigBool holdOrToggleConfig) {
			this.list = list;
			this.holdOrToggleConfig = holdOrToggleConfig;
			this.holdToggleButton = Button.builder(CommonComponents.EMPTY, button -> {
				holdOrToggleConfig.set(!holdOrToggleConfig.get());
				JojoMod.config.saveClient();
			})
			.bounds(0, 0, 75, 20)
			.build();
			this.buttonsAsList = new ArrayList<>(1);
			this.buttonsAsList.add(holdToggleButton);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return buttonsAsList;
		}

		@Override
		public void refreshEntry() {
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return buttonsAsList;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, 
				int mouseX, int mouseY, boolean hovering, float partialTick) {
			if (holdToggleButton != null) {
				Component message = Component.translatable(holdOrToggleConfig.get() ? "options.key.toggle" : "options.key.hold");
				holdToggleButton.setMessage(message != null ? message : CommonComponents.EMPTY);
				
				int scrollbarPosition = 
						list.getX() + list.getWidth() / 2 - list.getRowWidth() / 2
						+ list.getRowWidth()
						+ 10 /* getListOutlinePadding() */;
				int x = scrollbarPosition - 65 - holdToggleButton.getWidth();
				int y = top - 2;
				holdToggleButton.setPosition(x, y);
				holdToggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
			}
		}

	}


	public static interface KeyEntryDuck {
		void jojo_ripples$addButtonToTheLeftOfKey(Button button);
		void jojo_ripples$addTooltipDescription(Component description);
		void jojo_ripples$invokeRefreshEntry();
	}

}
