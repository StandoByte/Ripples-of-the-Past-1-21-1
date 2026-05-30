package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout.Orientation;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;

public class ToggleSwitch extends AbstractWidget {
	public static final GuiIcon VERTICAL_ON = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_v_on.png"), 16, 32);
	public static final GuiIcon VERTICAL_OFF = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_v_off.png"), 16, 32);
	public static final GuiIcon VERTICAL_ON_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_v_on_hovered.png"), 16, 32);
	public static final GuiIcon VERTICAL_OFF_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_v_off_hovered.png"), 16, 32);
	public static final GuiIcon HORIZONTAL_ON = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_h_on.png"), 32, 16);
	public static final GuiIcon HORIZONTAL_OFF = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_h_off.png"), 32, 16);
	public static final GuiIcon HORIZONTAL_ON_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_h_on_hovered.png"), 32, 16);
	public static final GuiIcon HORIZONTAL_OFF_HOVERED = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/widget/toggle_switch_h_off_hovered.png"), 32, 16);
	
	static final GuiIcon[] SPRITES = new GuiIcon[] { 
			VERTICAL_ON,
			VERTICAL_OFF,
			VERTICAL_ON_HOVERED,
			VERTICAL_OFF_HOVERED,
			HORIZONTAL_ON,
			HORIZONTAL_OFF,
			HORIZONTAL_ON_HOVERED,
			HORIZONTAL_OFF_HOVERED,
	};
	public static GuiIcon getSprite(Orientation orientation, boolean isHovered, boolean toggleState) {
		int index = 
				(orientation == Orientation.HORIZONTAL ? 1 : 0) << 2
				| (isHovered ? 1 : 0) << 1
				| (!toggleState ? 1 : 0);
		return SPRITES[index];
	}
	
	protected Orientation orientation;
	@Nullable protected final Supplier<Boolean> stateGet;
	@Nullable protected final Consumer<Boolean> stateSet;
	protected boolean stateDefault;

	public ToggleSwitch(int x, int y, Orientation orientation, 
			Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, @Nullable Tooltip tooltip) {
		super(x, y, 
				orientation == Orientation.VERTICAL ? 16 : 32, 
				orientation == Orientation.VERTICAL ? 32 : 16, 
				CommonComponents.EMPTY);
		this.orientation = orientation;
		this.stateGet = stateGet;
		this.stateSet = stateSet;
		this.stateDefault = stateGet.get();
		setTooltip(tooltip);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		toggle();
	}

	public void toggle() {
		if (stateSet != null) {
			stateSet.accept(!getState());
		}
		stateDefault = !stateDefault;
	}

	public boolean getState() {
		return stateGet != null ? stateGet.get() : stateDefault;
	}

	public void updateFromState() {
		if (stateSet != null) {
			stateSet.accept(getState());
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		GuiIcon sprite = getSprite(orientation, isHovered(), getState());
		int x = getX();
		int y = getY();
		sprite.render(guiGraphics.pose(), x, y);
	}

	
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}
	
}
