package com.github.standobyte.jojo.client.ui.hud;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.client.ClientEntityController;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class AdditionalHud {
	public static ExtrasHudLayer instance;

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent event) {
		event.registerAboveAll(JojoMod.resLoc("extra_hud"), instance = new ExtrasHudLayer());
	}
	
	public static class ExtrasHudLayer implements LayeredDraw.Layer {
		
		@Override
		public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui) return;
			
			var entityControl = ClientEntityController.getInstance();
			if (entityControl != null) {
				entityControl.renderExtraHud(guiGraphics, deltaTracker);
			}
			
			float partialTick = ClientUtil.partialTick(deltaTracker, true);
			BottomLeftNotifications.render(guiGraphics, partialTick);
		}
	}
}
