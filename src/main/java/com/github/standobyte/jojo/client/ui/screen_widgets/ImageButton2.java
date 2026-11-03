package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;

public class ImageButton2 extends Button {
	public WidgetSprites2 sprites;

	public ImageButton2(int x, int y, int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Button.OnPress onPress) {
		this(x, y, width, height, 
				enabled, disabled, enabledFocused, disabledFocused, 
				onPress, null);
	}

	public ImageButton2(int x, int y, int width, int height, 
			GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused, 
			Button.OnPress onPress, Tooltip tooltip) {
		this(x, y, width, height, 
				new WidgetSprites2(enabled, disabled, enabledFocused, disabledFocused),
				onPress, tooltip);
	}

	public ImageButton2(int x, int y, int width, int height, 
			WidgetSprites2 sprites, 
			Button.OnPress onPress, Tooltip tooltip) {
		super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		setTooltip(tooltip);
		this.sprites = sprites;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		GuiIcon icon = sprites.get(this);
		if (icon != null) {
			icon.render(guiGraphics.pose(), this.getX(), this.getY(), this.width, this.height, BlitFloat.NO_TINT);
		}
	}

	
	public static final WidgetSprites2 LEFT_ARROW = new WidgetSprites2(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/left.png"),				11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/left_inactive.png"),		11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/left_hovered.png"),		11, 11));
	public static final WidgetSprites2 RIGHT_ARROW = new WidgetSprites2(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/right.png"),				11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/right_inactive.png"),	11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/right_hovered.png"),		11, 11));
	public static final WidgetSprites2 UP_ARROW = new WidgetSprites2(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/up.png"),				11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/up_inactive.png"),		11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/up_hovered.png"),		11, 11));
	public static final WidgetSprites2 DOWN_ARROW = new WidgetSprites2(
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/down.png"),				11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/down_inactive.png"),		11, 11),
			new GuiIcon(JojoMod.resLoc("textures/gui/sprites/arrow/down_hovered.png"),		11, 11));
	public static class WidgetSprites2 {
		public GuiIcon enabled;
		public GuiIcon disabled;
		public GuiIcon enabledFocused;
		public GuiIcon disabledFocused;

		public WidgetSprites2(GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused, GuiIcon disabledFocused) {
			this.enabled = enabled;
			this.disabled = disabled;
			this.enabledFocused = enabledFocused;
			this.disabledFocused = disabledFocused;
		}

		public WidgetSprites2(GuiIcon enabled, GuiIcon disabled, GuiIcon enabledFocused) {
			this(enabled, disabled, enabledFocused, disabled);
		}

		public GuiIcon get(AbstractWidget widget) {
			return get(widget.isActive(), widget.isHoveredOrFocused());
		}

		public GuiIcon get(boolean enabled, boolean focused) {
			if (enabled) {
				return focused ? this.enabledFocused : this.enabled;
			} else {
				return focused ? this.disabledFocused : this.disabled;
			}
		}
	}
}
