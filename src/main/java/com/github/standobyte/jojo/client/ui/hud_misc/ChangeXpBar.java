package com.github.standobyte.jojo.client.ui.hud_misc;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ChangeXpBar {
	public static final int WIDTH = 182;
	public static final int HEIGHT = 5;
	public static final int DOWN_OFFSET = 29;

	
	public static boolean disableVanillaXpBar = false;
	
	public static boolean isXpBarVisible() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && mc.player.jumpableVehicle() == null && mc.gameMode.hasExperience();
	}
	
	public static void renderXpBar(GuiGraphics guiGraphics,
			ResourceLocation backgroundTexture, ResourceLocation progressTexture, 
			float progress, String level, int levelColor, @Nullable GuiIcon levelIcon) {
		Minecraft mc = Minecraft.getInstance();
		PoseStack poseStack = guiGraphics.pose();

		mc.getProfiler().push("expBar");
		int barX = (int) (guiGraphics.guiWidth() - WIDTH) / 2;
		int barY = (int) guiGraphics.guiHeight() - DOWN_OFFSET;
		int progressWidth = (int)(progress * (WIDTH) + 1);
		RenderSystem.enableBlend();
		BlitFloat.blit(poseStack, mc, backgroundTexture, 
				barX, barY, WIDTH, HEIGHT, 0, BlitFloat.NO_TINT);
		if (progressWidth > 0) {
			BlitFloat.blit(poseStack, mc, progressTexture, 
					barX, barY, progressWidth, HEIGHT, 0, 
					0, 0, progressWidth, HEIGHT, WIDTH, HEIGHT, BlitFloat.NO_TINT);
		}

		RenderSystem.disableBlend();
		mc.getProfiler().pop();

		Font font = mc.font;

		mc.getProfiler().push("expLevel");
		if (!level.isEmpty()) {
			int levelX = (guiGraphics.guiWidth() - font.width(level)) / 2;
			int levelY = barY - 6;
			if (levelIcon != null) {
				float iconX = levelX - levelIcon.width / 2;
				float iconY = levelY - (levelIcon.height - font.lineHeight) / 2 - 1;
				levelIcon.render(poseStack, iconX, iconY);
				levelX += levelIcon.width / 2 + 2;
			}
			guiGraphics.drawString(font, level, levelX + 1, levelY, 0, false);
			guiGraphics.drawString(font, level, levelX - 1, levelY, 0, false);
			guiGraphics.drawString(font, level, levelX, levelY + 1, 0, false);
			guiGraphics.drawString(font, level, levelX, levelY - 1, 0, false);
			guiGraphics.drawString(font, level, levelX, levelY, levelColor, false);
			mc.getProfiler().pop();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void frameClear(RenderFrameEvent.Pre event) {
		disableVanillaXpBar = false;
	}

	@SubscribeEvent
	public static void onRenderXp(RenderGuiLayerEvent.Pre event) {
		if (disableVanillaXpBar) {
			ResourceLocation layerName = event.getName();
			boolean xpBar = layerName.equals(VanillaGuiLayers.EXPERIENCE_BAR);
			boolean xpLevel = !xpBar && layerName.equals(VanillaGuiLayers.EXPERIENCE_LEVEL);
			
			if (xpBar || xpLevel) {
				event.setCanceled(true);
			}
		}
	}
	
}
