package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ClothesSetButton extends Button {
	public final Component baseText;
	public final ClothesSet clothesSet;

	public ClothesSetButton(int pX, int pY, int pWidth, int pHeight, 
			Component pMessage, Button.OnPress pOnPress, ClothesSet clothesSet) {
		this(pX, pY, pWidth, pHeight, pMessage, pOnPress, null, clothesSet);
	}

	public ClothesSetButton(int pX, int pY, int pWidth, int pHeight, 
			Component pMessage, Button.OnPress pOnPress, Tooltip pOnTooltip, ClothesSet clothesSet) {
		super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
		setTooltip(pOnTooltip);
		this.clothesSet = clothesSet;
		this.baseText = pMessage;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
		Minecraft minecraft = Minecraft.getInstance();
		Font fontrenderer = minecraft.font;
		PoseStack poseStack = guiGraphics.pose();
		int x = getX();
		int y = getY();
		int i = !this.active ? 0 : this.isHovered() ? 2 : 1;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		BlitFloat.blit(poseStack, minecraft, SewingMachineScreen.TEXTURE, 
				x, y, width, height, 0, 
				352, 106 + i * height, width, height, 512, 512, 
				ARGB.white(this.alpha));
		int j = getFGColor();
		
		if (SewingMachineScreen.settingsInstance != null) {
			var holder = SewingMachineScreen.settingsInstance.getSelectedSet();
			if (holder != null && holder.value() == this.clothesSet) {
				BlitFloat.blit(poseStack, minecraft, SewingMachineScreen.TEXTURE, 
						x - 9, y, 4, height, 0, 
						349, 106 + 3 * height, 4, height, 512, 512, 
						ARGB.white(this.alpha));
			}
		}

		Component message = getMessage();

		renderScrollingString(guiGraphics, fontrenderer, message, 
				x + 2, y, 
				x + this.width - 2, y + this.height, 
				j | Mth.ceil(this.alpha * 255.0F) << 24);
		
//		int textX = x + this.width / 2;
//		int textY = y + (this.height - 8) / 2;
//		guiGraphics.drawCenteredString(fontrenderer, message, textX, textY, j | Mth.ceil(this.alpha * 255.0F) << 24);
	}

}
