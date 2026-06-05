package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

public class ToggleButton extends AbstractWidget {
	public static final GuiIcon VISIBILITY_ON = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/visibility_toggle_on.png"), 10, 10);
	public static final GuiIcon VISIBILITY_OFF = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/visibility_toggle_off.png"), 10, 10);
	public static final GuiIcon VISIBILITY_ON_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/visibility_toggle_on_hovered.png"), 10, 10);
	public static final GuiIcon VISIBILITY_OFF_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/visibility_toggle_off_hovered.png"), 10, 10);
	
	public GuiIcon spriteEnabled;
	public GuiIcon spriteDisabled;
	public GuiIcon spriteEnabledFocused;
	public GuiIcon spriteDisabledFocused;
	
	protected Supplier<Boolean> stateGet;
	protected Consumer<Boolean> stateSet;

	public static ToggleButton visibility(int x, int y, int width, int height, 
			Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, @Nullable Tooltip tooltip) {
		return new ToggleButton(x, y, width, height, 
				VISIBILITY_ON, VISIBILITY_OFF, VISIBILITY_ON_HOVERED, VISIBILITY_OFF_HOVERED, 
				stateGet, stateSet, tooltip);
	}

	public ToggleButton(int x, int y, int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, @Nullable Tooltip tooltip) {
		super(x, y, width, height, CommonComponents.EMPTY);
		if (tooltip != null) {
			setTooltip(tooltip);
		}
		this.spriteEnabled = enabled;
		this.spriteDisabled = disabled;
		this.spriteEnabledFocused = enabledFocused;
		this.spriteDisabledFocused = disabledFocused;
		this.stateGet = stateGet;
		this.stateSet = stateSet;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		toggle();
	}

	public void toggle() {
		if (stateSet != null) {
			stateSet.accept(!getState());
		}
	}

	public boolean getState() {
		return stateGet.get();
	}

	public void updateFromState() {
		if (stateSet != null) {
			stateSet.accept(getState());
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		GuiIcon sprite = getSprite();
		int x = getX();
		int y = getY();
		sprite.render(guiGraphics.pose(), x, y);
	}

	public GuiIcon getSprite() {
		boolean isHovered = isActive() && isHovered();
		boolean state = getState();
		if (isHovered) {
			return state ? spriteEnabledFocused : spriteDisabledFocused;
		}
		else {
			return state ? spriteEnabled : spriteDisabled;
		}
	}
	
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

}
