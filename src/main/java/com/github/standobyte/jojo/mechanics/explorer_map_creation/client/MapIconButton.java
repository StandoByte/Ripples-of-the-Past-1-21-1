package com.github.standobyte.jojo.mechanics.explorer_map_creation.client;

import java.util.Optional;

import com.github.standobyte.jojo.init.ModMapDecorations;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class MapIconButton extends Button {
	public static final int SIZE = 16;
	protected boolean largeIcon;
	protected TextureAtlasSprite sprite;

	public MapIconButton(int x, int y, Button.OnPress onPress) {
		super(x, y, SIZE, SIZE, CommonComponents.EMPTY, onPress, Button.DEFAULT_NARRATION);
	}
	
	public void setIcon(Holder<MapDecorationType> iconType, TextureAtlasSprite sprite) {
		this.largeIcon = ModMapDecorations.LARGE_ICONS.contains(iconType);
		this.sprite = sprite;
		setMessage(Optional.ofNullable(UtilFunctions.getId(iconType))
				.map(id -> (Component) Component.literal(id.getPath()))
				.orElse(CommonComponents.EMPTY));
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		int x = getX();
		int y = getY();
		if (sprite != null) {
			PoseStack poseStack = guiGraphics.pose();
			poseStack.pushPose();
			poseStack.translate(x + SIZE / 2, y + SIZE / 2, 0);
			if (largeIcon) {
				poseStack.scale(2, 2, 1);
			}
			guiGraphics.blit(-4, -4, 0, 8, 8, sprite);
			poseStack.popPose();
		}
		if (isHoveredOrFocused()) {
			guiGraphics.fillGradient(RenderType.guiOverlay(), 
					x, y, x + 16, y + 16, 
					0x80FFFFFF, 0x80FFFFFF, 0);
		}
	}
}
