package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.datapack.DataDrivenStandsLoader;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModDataPackResources {

	@SubscribeEvent
	public static void addDataPackManagers(/*AddServerReloadListenersEvent*/AddReloadListenerEvent event) {
//		event.addListener(JojoMod.resLoc("stands"), DataDrivenStandsLoader.getDatapackStandsLoader());
		event.addListener(DataDrivenStandsLoader.getDatapackStandsLoader());
	}
	
	@SubscribeEvent
	public static void syncDataPack(OnDatapackSyncEvent event) {
		MinecraftServer server = event.getPlayerList().getServer();
		DataDrivenStandsLoader.syncDatapackTo(event.getRelevantPlayers(), server);
	}
	
}
