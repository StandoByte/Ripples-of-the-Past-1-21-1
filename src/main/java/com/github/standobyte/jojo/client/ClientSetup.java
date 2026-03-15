package com.github.standobyte.jojo.client;

import java.io.File;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.itemrender.ModItemModelOverrides;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.StandHudElements;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
	
	@SubscribeEvent
	public static void veryEarlyClientSetup(RegisterClientReloadListenersEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ClientModSettings.init(new File(mc.gameDirectory, "config/jojo_rotp/client_settings.json"));
//		HudControlSettings.init(new File(mc.gameDirectory, "config/jojo_rotp/controls/"));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onClientSetup0(FMLClientSetupEvent event) {
		JojoMenuTabs.initDefaults();
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ModMarkers.registerMarkers(mc);
		StandHudElements.init();
		event.enqueueWork(() -> {
			ModItemModelOverrides.register();
		});
	}
	
	@SubscribeEvent
	public static void registerKeyBindings(RegisterKeyMappingsEvent event) {
		InputHandler.init(event);
	}
}
