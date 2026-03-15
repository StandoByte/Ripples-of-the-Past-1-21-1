package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlaceholderScreen extends Screen implements IJojoMenuScreen {
	public static final ResourceLocation WINDOW = JojoMod.resLoc("textures/gui/paper_style/empty2.png");
	
	protected ResourceLocation texture;
	protected TabCategory category;
	protected Tab tab;

	public PlaceholderScreen(Component title, TabCategory category, Tab tab) {
		this(title, category, tab, WINDOW);
	}

	public PlaceholderScreen(Component title, TabCategory category, Tab tab, ResourceLocation texture) {
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);

		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
		
		renderTabs(guiGraphics, this);
		renderTabTooltip(guiGraphics, this, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

}
