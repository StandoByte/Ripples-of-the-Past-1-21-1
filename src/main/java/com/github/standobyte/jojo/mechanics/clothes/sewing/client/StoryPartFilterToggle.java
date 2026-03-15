package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import java.util.function.Consumer;

import com.github.standobyte.jojo.client.ui.screen_widgets.ToggleBox;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.google.common.base.Supplier;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;

public class StoryPartFilterToggle extends ToggleBox {
	protected final StoryPart part;

	public StoryPartFilterToggle(int x, int y, int width, int height, 
			Supplier<Boolean> stateGet, Consumer<Boolean> stateSet, StoryPart part) {
		super(x, y, width, height, CommonComponents.EMPTY, stateGet, stateSet, 
				Tooltip.create(part.getPartName()));
		this.part = part;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		PoseStack poseStack = guiGraphics.pose();
		int texX = 0;
		int x = getX();
		int y = getY();
		if (getState()) texX += width;
		if (isHovered()) texX += width * 2;
		BlitFloat.blit(poseStack, mc, SewingMachineScreen.TEXTURE, 
				x, y, width, height, 0, 
				texX, 258, 512, 512, 512, 512, 
				BlitFloat.NO_TINT);
		
		BlitFloat.blit(poseStack, mc, part.getPartIcon(), 
				x + 1, y + 1, width, height, 0, 
				0, 0, 16, 16, 16, 16, 
				BlitFloat.NO_TINT);
	}

}
