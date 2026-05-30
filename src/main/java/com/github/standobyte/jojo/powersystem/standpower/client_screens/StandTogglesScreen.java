package com.github.standobyte.jojo.powersystem.standpower.client_screens;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.VanillaKeybinds;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleButton;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleSwitch;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class StandTogglesScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/empty3.png");
	
	protected ResourceLocation texture;
	protected TabCategory category;
	protected Tab tab;
	
	protected ToggleSwitch testToggle;

	public StandTogglesScreen(Component title, TabCategory category, Tab tab) {
		this(title, category, tab, WINDOW);
	}

	public StandTogglesScreen(Component title, TabCategory category, Tab tab, ResourceLocation texture) {
		super(title);
		this.category = category;
		this.tab = tab;
		this.texture = texture;
	}
	
	
	public static ToggleEntry breakBlocks;
	public static ToggleEntry[] toggles;
	
	public static ToggleEntry[] lazyInitToggles() {
		if (toggles == null) {
			var client = JojoMod.config.getClient();
			var broadcastClient = JojoMod.config.getPlayerBroadcast(Minecraft.getInstance().player);
			var common = JojoMod.config.getCommon();
			
			breakBlocks = new ToggleEntry(
					broadcastClient.standsBreakBlocks, common.standsBreakBlocks, 
					ModConfigType.CLIENT_BROADCAST, 
					client.toggleVisible_standsBreakBlocks, () -> true, 
					keybinds -> keybinds.standToggle_breakBlocks, 
					ConfigGuiHelper.toIconPath("stands_break_blocks"), Component.translatable("jojo_ripples.stand_toggles.destroy_blocks"));

			toggles = new ToggleEntry[] {
					breakBlocks
			};
		}
		return toggles;
	}
	
	

	@Override
	public TabCategory getTabCategory() {
		return category;
	}

	@Override
	public Tab getTab() {
		return tab;
	}
	
	@Override
	public void init() {
		super.init();

		ToggleEntry[] toggles = lazyInitToggles();
		int x = getWindowX(this);
		int y = getWindowY(this) + 8;
		for (ToggleEntry toggle : toggles) {
			addToggleUI(toggle, x, y);
			y += 24;
		}
		
	}

	protected ToggleEntry addToggleUI(ToggleEntry entry, int x, int y) {
		entry.init(x, y);
		addWidget(entry.toggle);
		addWidget(entry.keybindButton);
		addWidget(entry.visibilityToggle);
		addRenderableOnly(entry);
		return entry;
	}
	
	public static class ToggleEntry implements Renderable {
		public final ConfigOption<Boolean> setting;
		@Nullable public final ConfigOption<BoolOrPlayerPref> overrulingCommonSetting;
		public final ModConfigType configToSave;
		public final Component text;
		
		public final ConfigOption<Boolean> hudVisibilitySetting;
		public final GuiIcon hudIcon;
		public final BooleanSupplier activeWhen;
		
		public final Function<VanillaKeybinds, KeyMapping> keybind;
		
		public ToggleSwitch toggle;
		public Button keybindButton;
		public ToggleButton visibilityToggle;
		
		public ToggleEntry(ConfigOption<Boolean> setting, @Nullable ConfigOption<BoolOrPlayerPref> overrulingCommonSetting,
				ModConfigType configToSave, 
				ConfigOption<Boolean> hudVisibilitySetting, BooleanSupplier renderInHudWhen, 
				Function<VanillaKeybinds, KeyMapping> keybind, 
				ResourceLocation icon, Component text) {
			this.setting = setting;
			this.overrulingCommonSetting = overrulingCommonSetting;
			this.configToSave = configToSave;
			this.keybind = keybind;
			this.text = ConfigGuiHelper.prependIcon(text, icon);
			
			this.hudVisibilitySetting = hudVisibilitySetting;
			this.hudIcon = new GuiIcon(icon, 16, 16);
			this.activeWhen = renderInHudWhen;
		}
		
		public void init(int x, int y) {
			keybindButton = Button.builder(CommonComponents.EMPTY, b -> {})
					.bounds(x + 155, y, 50, 20)
					.createNarration(
							message -> false
							? Component.translatable("narrator.controls.unbound", text)
							: Component.translatable("narrator.controls.bound", text, message.get()))
					.build();
			
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
			keybindButton.render(guiGraphics, mouseX, mouseY, partialTick);
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

	@Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    	super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
    }

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);
		
		renderTabs(guiGraphics, this);
		renderTabTooltip(guiGraphics, this, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

}
