package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Tab implements IJojoMenuTab {
	protected TabCategory category;
	protected boolean isDisabled = false;
	protected @Nullable PowerClass<?> powerClass;
	protected @Nullable Supplier<? extends PowerType> powerType;
	
	public Tab(TabCategory category) {
		this(category, null, null);
	}
	
	protected Tab(TabCategory category, @Nullable PowerClass<?> powerClass, @Nullable Supplier<? extends PowerType> powerType) {
		this.category = category;
		category.tabs.add(this);
		this.powerClass = powerClass;
		this.powerType = powerType;
	}
	
	public Tab disable() {
		this.isDisabled = true;
		return this;
	}
	
	public boolean isActive() {
		if (isDisabled) return false;
		if (powerClass != null) {
			Power<?> power = ClientPowerCache.getPower(powerClass);
			return power != null && power.hasPower() && (powerType == null || power.getPowerType() == powerType.get());
		}
		return true;
	}
	
	public TabCategory getCategory() {
		return category;
	}
	
	
	protected Function<Tab, ? extends Screen> newScreen = tab -> new PlaceholderScreen(Component.empty(), this.getCategory(), this);
	
	public Tab withScreen(Function<Tab, ? extends Screen> newScreen) {
		this.newScreen = newScreen;
		return this;
	}


	@Override
	public Tab getTabToOpen() {
		return this;
	}
	
	public boolean onTabClick(Minecraft mc, Screen curScreen) {
		if (newScreen != null) {
			Screen screen = newScreen.apply(this);
			if (screen != null) {
				mc.setScreen(screen);
				JojoMenuTabs.onTabOpened(this);
				return true;
			}
		}
		return false;
	}
	
	
	protected Component name = Component.empty();
	public Tab withName(Component name) {
		Objects.requireNonNull(name);
		this.name = name;
		return this;
	}

	@Override
	public Component getName() {
		return name;
	}
	
	
	protected GuiIcon icon;
	public Tab withIcon(GuiIcon icon) {
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
