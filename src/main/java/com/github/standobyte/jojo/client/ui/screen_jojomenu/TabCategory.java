package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class TabCategory implements IJojoMenuTab {
	public static final List<TabCategory> ALL_CATEGORIES = new ArrayList<>();
	
	public TabCategory() {
		this(null, null);
	}
	
	public TabCategory(@Nullable PowerClass<?> powerClass, @Nullable Supplier<? extends PowerType> powerType) {
		this.powerClass = powerClass;
		this.powerType = powerType;
		ALL_CATEGORIES.add(this);
	}
	
	public static List<TabCategory> getActiveCategories() {
		return ALL_CATEGORIES.stream().filter(TabCategory::isActive).toList();
	}
	
	protected final @Nullable PowerClass<?> powerClass;
	protected final @Nullable Supplier<? extends PowerType> powerType;
	protected final List<Tab> tabs = new ArrayList<>();
	
	public boolean isActive() {
		if (powerClass != null) {
			Power<?> power = ClientPowerCache.getPower(powerClass);
			return power != null && power.hasPower() && (powerType == null || power.getPowerType() == powerType.get());
		}
		for (Tab tab : tabs) {
			if (tab.isActive()) {
				return true;
			}
		}
		return false;
	}
	
	public List<Tab> getActiveTabs() {
		return tabs.stream().filter(Tab::isActive).toList();
	}


	@Override
	public Tab getTabToOpen() {
		return JojoMenuTabs.getTabToOpen(this);
	}


	protected Component name = Component.empty();
	public TabCategory withName(Component name) {
		this.name = name;
		return this;
	}

	@Override
	public Component getName() {
		return name;
	}
	
	
	protected GuiIcon icon;
	public TabCategory withIcon(GuiIcon icon) {
		this.icon = icon;
		return this;
	}
	
	@Nullable
	public GuiIcon getIcon() {
		return icon;
	}

	@Override
	public void renderIcon(GuiGraphics guiGraphics, int x, int y) {
		GuiIcon icon = getIcon();
		if (icon != null) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			icon.render(guiGraphics.pose(), x, y);
			RenderSystem.disableBlend();
		}
	}
	
}
