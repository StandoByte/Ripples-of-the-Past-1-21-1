package com.github.standobyte.jojo.powersystem.standpower.client_screens;

import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleButton;
import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleSwitch;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LinearLayout.Orientation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
			breakBlocks = new ToggleEntry(
					broadcastClient.standsBreakBlocks, client.toggleVisible_standsBreakBlocks, 
					ModConfigType.CLIENT_BROADCAST, 
					ConfigGuiHelper.prependIcon(
							Component.translatable("jojo_ripples.stand_toggles.destroy_blocks"), 
							ConfigGuiHelper.toIconPath("stands_break_blocks")));

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
		public ConfigBool setting;
		public ConfigBool visibilitySetting;
		public ModConfigType configToSave;
		public Component text;
		
		public ToggleSwitch toggle;
		public Button keybindButton;
		public ToggleButton visibilityToggle;
		
		public ToggleEntry(ConfigBool setting, ConfigBool visibilitySetting, ModConfigType configToSave, Component text) {
			this.setting = setting;
			this.visibilitySetting = visibilitySetting;
			this.configToSave = configToSave;
			this.text = text;
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
					}, null);
			
			visibilityToggle = ToggleButton.visibility(x + 211, y + 5, 10, 10, 
					visibilitySetting, 
					newVal -> {
						visibilitySetting.set(newVal);
						ConfigGuiHelper.onSettingChange(JojoMod.config, ModConfigType.CLIENT, visibilitySetting);
					}, null);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			toggle.render(guiGraphics, mouseX, mouseY, partialTick);
			// TODO scrolling string
			guiGraphics.drawString(Minecraft.getInstance().font, text, 
					toggle.getX() + 32, toggle.getY() + 4, 0xFF000000, false);
			keybindButton.render(guiGraphics, mouseX, mouseY, partialTick);
			visibilityToggle.render(guiGraphics, mouseX, mouseY, partialTick);
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
