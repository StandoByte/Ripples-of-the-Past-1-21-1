package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.client.ui.PlayerClothesScreen;
import com.github.standobyte.jojo.mechanics.clothes.sewing.client.SewingMachineScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ModContainerScreensClient {

	@SubscribeEvent
	public static void registerScreens(RegisterMenuScreensEvent event) {
		event.register(ModContainers.PLAYER_CLOTHES.get(), PlayerClothesScreen::new);
		event.register(ModContainers.SEWING_MACHINE.get(), SewingMachineScreen::new);
	}
	
}
