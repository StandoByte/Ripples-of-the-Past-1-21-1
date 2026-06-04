package com.github.standobyte.jojo.config.stand_toggles;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.ui.KeybindsEditingUI;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleButton;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleSwitch;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.config.BoolOrPlayerPref;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout.Orientation;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ClientStandToggle implements Renderable {
	public final ConfigOption<Boolean> setting;
	@Nullable public final ConfigOption<BoolOrPlayerPref> overrulingCommonSetting;
	public final ModConfigType configToSave;
	public final Component text;
	
	public final ConfigOption<Boolean> hudVisibilitySetting;
	public final GuiIcon hudIcon;
	@Nullable public final GuiIcon offHudIcon;
	public final BooleanSupplier activeWhen;
	
	public final Function<VanillaKeybinds, KeyMapping> keybind;
	
	public ToggleSwitch toggle;
	public Button keybindButton;
	public ToggleButton visibilityToggle;
	
	public ClientStandToggle(ConfigOption<Boolean> setting, @Nullable ConfigOption<BoolOrPlayerPref> overrulingCommonSetting,
			ModConfigType configToSave, 
			ConfigOption<Boolean> hudVisibilitySetting, BooleanSupplier renderInHudAndUseKeybindWhen, 
			Function<VanillaKeybinds, KeyMapping> keybind, 
			GuiIcon icon, @Nullable GuiIcon offIcon, Component text) {
		this.setting = setting;
		this.overrulingCommonSetting = overrulingCommonSetting;
		this.configToSave = configToSave;
		this.keybind = keybind;
		this.text = ConfigGuiHelper.prependIcon(text, icon);
		
		this.hudVisibilitySetting = hudVisibilitySetting;
		this.hudIcon = icon;
		this.offHudIcon = offIcon;
		this.activeWhen = renderInHudAndUseKeybindWhen;
	}
	
	public void init(int x, int y, KeybindsEditingUI keybindsHandler) {
		KeyMapping keyMapping = keybind.apply(InputHandler.getInstance().vanillaKeybinds);
		if (keyMapping != null) {
			keybindButton = keybindsHandler.addKeybind(keyMapping, JojoMod.config::saveClient, x + 155, y, 50, 20).button;
		}
		
		toggle = new ToggleSwitch(x + 4, y + 2, Orientation.HORIZONTAL, 
				setting, 
				newVal -> {
					setting.set(newVal);
					ConfigGuiHelper.onSettingChange(JojoMod.config, configToSave, setting);
				}, null) {
			
			@Override
			public boolean getStateToRender() {
				return getResultingValue();
			}
		};
		
		visibilityToggle = ToggleButton.visibility(x + 211, y + 5, 10, 10, 
				hudVisibilitySetting, 
				newVal -> {
					hudVisibilitySetting.set(newVal);
					ConfigGuiHelper.onSettingChange(JojoMod.config, ModConfigType.CLIENT, hudVisibilitySetting);
				}, null);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		toggle.active = clientCanToggle();
		MutableComponent toggleTooltip;
		if (!clientCanToggle()) {
			toggleTooltip = Component.translatable("jojo_ripples.toggle_overruled", 
					overrulingCommonSetting.get().optionStatus);
		}
		else {
			toggleTooltip = CommonComponents.optionStatus(getResultingValue()).copy();
		}
		toggle.setTooltip(Tooltip.create(toggleTooltip.withStyle(ChatFormatting.BLACK)));
		
		visibilityToggle.setTooltip(Tooltip.create(CommonComponents.optionStatus(
				Component.translatable("jojo_ripples.toggle.show_in_hud"), visibilityToggle.getState())
				.copy().withStyle(ChatFormatting.BLACK)));
		
		toggle.render(guiGraphics, mouseX, mouseY, partialTick);
		// TODO scrolling string
		guiGraphics.drawString(Minecraft.getInstance().font, text, 
				toggle.getX() + 32, toggle.getY() + 4, 0xFF000000, false);
		if (keybindButton != null) keybindButton.render(guiGraphics, mouseX, mouseY, partialTick);
		visibilityToggle.render(guiGraphics, mouseX, mouseY, partialTick);
		
		if (toggle.isHovered() || visibilityToggle.isHovered()) {
			TooltipParams.set(TooltipParams.paperStyle());
		}
	}
	
	public void toggle() {
		setting.set(!setting.get());
		ConfigGuiHelper.onSettingChange(JojoMod.config, configToSave, setting);
	}
	
	public boolean getResultingValue() {
		if (overrulingCommonSetting != null) {
			Boolean commonValue = overrulingCommonSetting.get().asBoolean;
			if (commonValue != null) {
				return commonValue;
			}
		}
		
		return setting.get();
	}
	
	public boolean clientCanToggle() {
		return overrulingCommonSetting == null || overrulingCommonSetting.get() == BoolOrPlayerPref.PLAYER_PREFERENCE;
	}
	
}
