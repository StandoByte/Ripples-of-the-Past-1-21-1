package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public interface IJojoMenuScreen {
	public static final int DEFAULT_WIDTH = 230;
	public static final int DEFAULT_HEIGHT = 227;
	public static final ResourceLocation TABS_TEXTURE = JojoMod.resLoc("textures/gui/paper_style/screen_tabs.png");
	
	TabCategory getTabCategory();
	
	Tab getTab();
	
	default int getWindowX(Screen screen) { return (screen.width  - DEFAULT_WIDTH) / 2; }
	default int getWindowY(Screen screen) { return (screen.height - DEFAULT_HEIGHT) / 2; }
	default int getWindowWidth() { return DEFAULT_WIDTH; }
	default int getWindowHeight() { return DEFAULT_HEIGHT; }
	
	public static final int TAB_WIDTH = 28;
	public static final int TAB_LENGTH = 32;
	
	default boolean rightSideTabsEnabled() { return true; }
	
	public default void renderTabs(GuiGraphics guiGraphics, Screen screen) {
		int x = getWindowX(screen);
		int y = getWindowY(screen);
		int width = getWindowWidth();
		TabCategory curCategory = getTabCategory();
		Tab curTab = getTab();
		
		int tabX = x - TAB_LENGTH + 4;
		int tabY = y;
		boolean firstTab = true;
		for (TabCategory category : TabCategory.getActiveCategories()) {
			float texX = firstTab ? 0 : TAB_LENGTH;
			float texY = TAB_LENGTH * 2 + (category == curCategory ? TAB_WIDTH : 0);
			BlitFloat.blit(guiGraphics.pose(), screen.getMinecraft(), TABS_TEXTURE, 
					tabX, tabY, TAB_LENGTH, TAB_WIDTH, 0, 
					texX, texY, TAB_LENGTH, TAB_WIDTH, 256, 256, 
					BlitFloat.NO_TINT);
			category.renderIcon(guiGraphics, tabX + 10, tabY + 6);
			tabY += TAB_WIDTH;
			firstTab = false;
		}

		if (rightSideTabsEnabled()) {
			tabX = x + width - 4;
			tabY = y;
			firstTab = true;
			for (Tab tab : curCategory.getActiveTabs()) {
				float texX = TAB_LENGTH * 3 + (firstTab ? 0 : TAB_LENGTH);
				float texY = TAB_LENGTH * 2 + (tab == curTab ? TAB_WIDTH : 0);
				BlitFloat.blit(guiGraphics.pose(), screen.getMinecraft(), TABS_TEXTURE, 
						tabX, tabY, TAB_LENGTH, TAB_WIDTH, 0, 
						texX, texY, TAB_LENGTH, TAB_WIDTH, 256, 256, 
						BlitFloat.NO_TINT);
				tab.renderIcon(guiGraphics, tabX + 6, tabY + 6);
				tabY += TAB_WIDTH;
				firstTab = false;
			}
		}
	}
	
	default void renderTabTooltip(GuiGraphics guiGraphics, Screen screen, int mouseX, int mouseY) {
		IJojoMenuTab tab = getTabAt(mouseX, mouseY, screen);
		if (tab != null) {
			Component name = tab.getName();
			if (name != null) {
				screen.setTooltipForNextRenderPass(((MutableComponent) name).withStyle(ChatFormatting.BLACK));
				TooltipParams.set(TooltipParams.paperStyle());
//				guiGraphics.renderTooltip(_Screen.getFont(screen), name, mouseX, mouseY);
			}
		}
	}
	
	default boolean clickTab(double mouseX, double mouseY, int button, Screen screen) {
		if (button == 0) {
			IJojoMenuTab tabWidget = getTabAt(mouseX, mouseY, screen);
			if (tabWidget != null) {
				Tab tabToOpen = tabWidget.getTabToOpen();
				boolean playSound = tabToOpen != null && !JojoMenuTabs.isSameTabOpened(tabToOpen);
				if (tabWidget.onClick(screen.getMinecraft(), screen)) {
					if (playSound) {
						SoundManager handler = Minecraft.getInstance().getSoundManager();
						handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
					}
					
					return true;
				}
			}
		}
		return false;
	}
	
	@Nullable
	default public IJojoMenuTab getTabAt(double mouseX, double mouseY, Screen screen) {
		int x = getWindowX(screen);
		int y = getWindowY(screen);
		int width = getWindowWidth();
		
		if (mouseY >= y) {
			if (mouseX < x && mouseX >= x - TAB_LENGTH) {
				int categoryIndex = (int) ((mouseY - y) / TAB_WIDTH);
				List<TabCategory> categories = TabCategory.getActiveCategories();
				if (categoryIndex < categories.size()) {
					return categories.get(categoryIndex);
				}
			}
			
			if (rightSideTabsEnabled()) {
				if (mouseX > x + width && mouseX <= x + width + TAB_LENGTH) {
					int tabIndex = (int) ((mouseY - y) / TAB_WIDTH);
					List<Tab> tabs = getTabCategory().getActiveTabs();
					if (tabIndex < tabs.size()) {
						return tabs.get(tabIndex);
					}
				}
			}
		}
		
		return null;
	}
	
}
