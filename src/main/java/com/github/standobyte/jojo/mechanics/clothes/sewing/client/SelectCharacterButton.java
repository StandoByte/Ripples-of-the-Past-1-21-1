package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import com.github.standobyte.jojo.client.ui.screen_widgets.FilterList;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SelectCharacterButton implements FilterList.Entry {
	final SewingMachineScreen screen;
	final ClothesCharacterUIEntry character;
	public int x;
	public int y;
	public boolean isVisible = true;

	public SelectCharacterButton(SewingMachineScreen screen, ClothesCharacterUIEntry character) {
		this.screen = screen;
		this.character = character;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public void addY(int addY) {
		this.y += addY;
	}

	@Override
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	public void render(GuiGraphics guiGraphics, Minecraft mc, int mouseX, int mouseY, float partialTick) {
		if (isVisible) {
			PoseStack poseStack = guiGraphics.pose();
			int texX = 0;
			if (this.character.getCharacter() == screen.getSettings().getSelectedCharacter()) {
				texX += 18;
			}
			else if (isMouseOver(mouseX, mouseY)) {
				texX += 36;
			}
			BlitFloat.blit(poseStack, mc, SewingMachineScreen.TEXTURE, 
					x, y, 18, 18, 0, 
					texX, 276, 18, 18, 512, 512, 
					BlitFloat.NO_TINT);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
		if (isMouseOver((int) mouseX, (int) mouseY)) {
			screen.getSettings().selectSet(screen, character.getSelectedSet());
		}
		return false;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX > x && mouseX <= x + 18 && mouseY > y && mouseY <= y + 18;
	}

}
