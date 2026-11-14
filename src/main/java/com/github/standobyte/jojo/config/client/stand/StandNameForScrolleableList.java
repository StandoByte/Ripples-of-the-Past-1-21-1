package com.github.standobyte.jojo.config.client.stand;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinColor;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class StandNameForScrolleableList extends ConfigGuiHelper.TextLine {
	@Nullable public GuiIcon icon;
	
	public static StandNameForScrolleableList fromDefaultSkin(StandType standType, Alignment alignment) {
		StandSkin standSkin = StandSkinsLoader.getInstance().getDefaultSkin(standType.getId());
		if (standSkin != null) {
			return new StandNameForScrolleableList(standSkin.getStandName(), 
					alignment, standSkin.getColors().text(), standSkin.getStandIcon());
		}
		else {
			return new StandNameForScrolleableList(standType.name.get(), 
					alignment, StandSkinColor.FALLBACK.text(), null);
		}
	}
	
	public StandNameForScrolleableList(Component standName, Alignment alignment, int color, @Nullable GuiIcon icon) {
		super(standName, alignment, color);
		this.icon = icon;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(20, 4, 0);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		poseStack.popPose();
		if (icon != null) {
			icon.render(poseStack, x, y);
		}
	}

}
