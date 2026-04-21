package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.itemrender.ModItemModelOverrides;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.StandHudElements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = JojoMod.MOD_ID, dist = Dist.CLIENT)
public class ClientSetup {
	
	public ClientSetup(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(this);
		
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, 
				(ModContainer container, Screen modListScreen) -> new ClientModSettingsScreen(modListScreen));
	}
	
//	@SubscribeEvent
//	public void veryEarlyClientSetup(RegisterClientReloadListenersEvent event) {
//		Minecraft mc = Minecraft.getInstance();
//	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClientSetup0(FMLClientSetupEvent event) {
		JojoMenuTabs.initDefaults();
	}

	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ModMarkers.registerMarkers(mc);
		StandHudElements.init();
		event.enqueueWork(() -> {
			ModItemModelOverrides.register();
		});
	}
	
	@SubscribeEvent
	public void registerKeyBindings(RegisterKeyMappingsEvent event) {
		InputHandler.init(event);
	}
}
