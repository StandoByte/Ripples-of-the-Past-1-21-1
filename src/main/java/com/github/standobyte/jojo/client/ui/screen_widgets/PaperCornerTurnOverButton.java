package com.github.standobyte.jojo.client.ui.screen_widgets;

import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.HumanoidArm;

public class PaperCornerTurnOverButton extends Button {
	public static final ResourceLocation CORNER_FOLD_TEX = JojoMod.resLoc("textures/gui/paper_style/corner_fold.png");
	public static final int WIDTH = 11;
	public static final int HEIGHT = 14;
	public static final GuiIcon TURN_OVER_LEFT = new GuiIcon(CORNER_FOLD_TEX, 0, 0, WIDTH, HEIGHT, 32, 32);
	public static final GuiIcon TURN_OVER_LEFT_HOVERED = new GuiIcon(CORNER_FOLD_TEX, 0, HEIGHT, WIDTH, HEIGHT, 32, 32);
	public static final GuiIcon TURN_OVER_RIGHT = new GuiIcon(CORNER_FOLD_TEX, WIDTH, 0, WIDTH, HEIGHT, 32, 32);
	public static final GuiIcon TURN_OVER_RIGHT_HOVERED = new GuiIcon(CORNER_FOLD_TEX, WIDTH, HEIGHT, WIDTH, HEIGHT, 32, 32);
	public HumanoidArm side;
	public boolean playTurnSound;

	public PaperCornerTurnOverButton(int x, int y, 
			Component narrationMessage, OnPress onPress, 
			HumanoidArm side, boolean playTurnSound) {
		this(x, y, narrationMessage, onPress, Button.DEFAULT_NARRATION, side, playTurnSound);
	}

	public PaperCornerTurnOverButton(int x, int y, 
			Component narrationMessage, OnPress onPress, CreateNarration createNarration, 
			HumanoidArm side, boolean playTurnSound) {
		super(x, y, WIDTH, HEIGHT, narrationMessage, onPress, createNarration);
		this.side = side;
		this.playTurnSound = playTurnSound;
	}

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    	boolean isActive = isActive();
    	boolean isHovered = isHoveredOrFocused();
    	if (isActive) {
    		GuiIcon icon = switch (side) {
    			case LEFT -> isHovered ? TURN_OVER_LEFT_HOVERED : TURN_OVER_LEFT;
    			case RIGHT -> isHovered ? TURN_OVER_RIGHT_HOVERED : TURN_OVER_RIGHT;
    		};
    		icon.render(guiGraphics.pose(), getX(), getY());
    	}
    }

    @Override
    public void playDownSound(SoundManager handler) {
    	if (playTurnSound) {
    		handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    	}
    }

}
