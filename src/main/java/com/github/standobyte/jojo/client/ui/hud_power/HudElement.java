package com.github.standobyte.jojo.client.ui.hud_power;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud.AbilityHud;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.client.ui.utils.tooltip.PowerHudHintTooltipHolder;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

public abstract class HudElement implements GuiEventListener {
	@ApiStatus.Internal
	public AbilityHud hud;
	public String name;
	public MultiLineScreenTooltip tooltipText;
    public PowerHudHintTooltipHolder tooltip = new PowerHudHintTooltipHolder();
	protected double xOffsetL;
	protected double xOffsetR;
	protected double yOffsetU;
	protected double yOffsetD;
	protected boolean isHovered;

	public SnappingH snappingHorizontal;
	public SnappingV snappingVertical;
//	@Nullable public double[] draggedAt;
	
	public HudElement(String name, int x0, int y0, int width, int height) {
		this(name, SnappingH.LEFT, SnappingV.UP, x0, y0, width, height);
	}
	
	public HudElement(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, int xOffset, int yOffset, int width, int height) {
		this.name = name;
		initText();
		this.snappingHorizontal = snappingHorizontal;
		this.snappingVertical = snappingVertical;
		switch (snappingHorizontal) {
			case LEFT -> this.xOffsetL = xOffset;
			case RIGHT -> this.xOffsetR = xOffset;
			default -> {}
		}
		switch (snappingVertical) {
			case UP -> this.yOffsetU = yOffset;
			case DOWN -> this.yOffsetD = yOffset;
			default -> {}
		}
		updateRectangle(width, height);
	}
	
	protected void initText() {
		this.tooltipText = new MultiLineScreenTooltip(
				Component.translatable("ripples_hud." + name).withStyle(ChatFormatting.BLACK), 
				Component.translatable("ripples_hud." + name + ".desc").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
		this.tooltip.set(this.tooltipText);
	}
	
	public void updateRectangle() {
		updateRectangle(rectangle.width(), rectangle.height());
	}
	
	public void updateRectangle(int width, int height) {
		int guiWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		int guiHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
		double x0 = switch (snappingHorizontal) {
			case LEFT -> this.xOffsetL;
			case RIGHT -> guiWidth - width - xOffsetR;
			case CENTER -> (guiWidth - width) / 2;
		};
		double y0 = switch (snappingVertical) {
			case UP -> this.yOffsetU;
			case DOWN -> guiHeight - height - yOffsetD;
			case CENTER -> (guiHeight - height) / 2;
		};
		this.xOffsetL = x0;
		this.xOffsetR = guiWidth - x0 - width;
		this.yOffsetU = y0;
		this.yOffsetD = guiHeight - y0 - height;
		this.rectangle = new ScreenRectangle(new ScreenPosition((int) x0, (int) y0), width, height);
	}

	
	public ScreenRectangle rectangle;
	@Override
	public ScreenRectangle getRectangle() {
		return rectangle;
	}
	
	public abstract boolean shouldRender();
	public abstract void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
	
	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, double mouseX, double mouseY) {
		updateRectangle();
		isHovered = guiGraphics.containsPointInScissor((int) mouseX, (int) mouseY)
				&& mouseX >= this.getX()
				&& mouseY >= this.getY()
				&& mouseX < this.getX() + this.getWidth()
				&& mouseY < this.getY() + this.getHeight();
		renderElement(guiGraphics, deltaTracker);
		checkTooltip(mouseX, mouseY, deltaTracker);
	}
	
	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		updateRectangle();
		renderElement(guiGraphics, deltaTracker);
	}
	
	protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
		tooltip.refreshTooltipForNextRenderPass(isHovered, this.isFocused(), this.getRectangle());
		if (tooltip.wasDisplayed) {
			TooltipParams.set(TooltipParams.paperStyle());
		}
	}
	
	
	public int getX() { return rectangle.left(); }
	public int getY() { return rectangle.top(); }
	public int getWidth() { return rectangle.width(); }
	public int getHeight() { return rectangle.height(); }
	
	public void setSize(int width, int height) {
		this.rectangle = new ScreenRectangle(rectangle.position(), width, height);
	}


//	@Override
//	public boolean mouseClicked(double mouseX, double mouseY, int button) {
//		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
////			setDraggedAt(mouseX, mouseY);
//			return true;
//		}
//		return false;
//	}
//	
////	protected void setDraggedAt(double mouseX, double mouseY) {
////		int guiWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
////		int guiHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
////		draggedAt = new double[] { mouseX - getX0(guiWidth), mouseY - getY0(guiHeight) };
////	}
//
//	@Override
//	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
////		if (draggedAt == null) {
////			setDraggedAt(mouseX, mouseY);
////		}
////		
//		this.xOffsetL += dragX;
//		this.xOffsetR += dragX;
//		this.yOffsetU += dragY;
//		this.yOffsetD += dragY;
//
//		// XXX (hud elements) limit dragging to screen edges
//		// XXX (hud elements) snap to center / right&down edges
//		
//		return true;
//	}
//
////	@Override
////	public boolean mouseReleased(double mouseX, double mouseY, int button) {
////		draggedAt = null;
////		return true;
////	}

	public enum SnappingH {
		LEFT,
		CENTER,
		RIGHT
	}

	public enum SnappingV {
		UP,
		CENTER,
		DOWN
	}


	protected boolean focused;
	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public boolean isFocused() {
		return focused;
	}
	
	
	public static boolean controlsHaveTypeAndAbility(PowerClass<?> powerClass, @Nullable PowerType specificPowerType) {
		Power<?> power = ClientPowerCache.getPower(powerClass);
		if (power != null) {
			PowerType powerType = power.getPowerType();
			if (powerType != null && (specificPowerType == null || powerType == specificPowerType)) {
				ClientControlScheme controlScheme = InputHandler.getInstance().getActiveControlScheme();
				return controlScheme != null && controlScheme.hasAbility(ability -> ability.powerClass() == powerClass);
			}
		}
		
		return false;
	}

}
