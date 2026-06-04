package com.github.standobyte.jojo.config.stand_toggles;

import com.github.standobyte.jojo.client.ui.KeybindsEditingUI;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StandTogglesScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/empty3.png");
	
	protected ResourceLocation texture;
	protected TabCategory category;
	protected Tab tab;
	
	protected KeybindsEditingUI keybindsHandler;

	public StandTogglesScreen(Component title, TabCategory category, Tab tab) {
		this(title, category, tab, WINDOW);
	}

	public StandTogglesScreen(Component title, TabCategory category, Tab tab, ResourceLocation texture) {
		super(title);
		this.category = category;
		this.tab = tab;
		this.texture = texture;
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
		
		keybindsHandler = new KeybindsEditingUI();

		ClientStandToggle[] toggles = ClientStandToggles.lazyInitToggles();
		int x = getWindowX(this);
		int y = getWindowY(this) + 8;
		for (ClientStandToggle toggle : toggles) {
			addToggleUI(toggle, x, y);
			y += 24;
		}
		keybindsHandler.refresh();
		
	}

	protected ClientStandToggle addToggleUI(ClientStandToggle entry, int x, int y) {
		entry.init(x, y, keybindsHandler);
		addWidget(entry.toggle);
		if (entry.keybindButton != null) {
			addWidget(entry.keybindButton);
		}
		addWidget(entry.visibilityToggle);
		addRenderableOnly(entry);
		return entry;
	}
	
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	return keybindsHandler.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }
	
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
    	return keybindsHandler.keyReleased(keyCode, scanCode, modifiers) || super.keyReleased(keyCode, scanCode, modifiers);
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
