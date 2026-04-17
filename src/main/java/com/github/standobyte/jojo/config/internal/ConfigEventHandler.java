package com.github.standobyte.jojo.config.internal;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.core.ModRotpConfigEvent;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ConfigEventHandler {
	public static final Map<String, ModConfig<?, ?, ?>> ALL_CONFIGS = new HashMap<>();
	
	
	@EventBusSubscriber(modid = JojoMod.MOD_ID)
	public static class EventHandler {
		
		@SubscribeEvent
		public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			ALL_CONFIGS.entrySet().forEach(configEntry -> {
				ModConfig<?, ?, ?> config = configEntry.getValue();
				ConfigNetworkFunctions.srvSendConfigsToLoggedInPlayer(player, 
						configEntry.getKey(), config);
			});
		}
		
		@SubscribeEvent
		public static void onServerPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			ALL_CONFIGS.entrySet().forEach(configEntry -> {
				ModConfig<?, ?, ?> config = configEntry.getValue();
				if (config.serverBroadcast != null) {
					config.serverBroadcast.remove(player.getUUID());
				}
			});
		}
		
		@SubscribeEvent
		public static void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
			ALL_CONFIGS.entrySet().forEach(configEntry -> {
				ModConfig<?, ?, ?> config = configEntry.getValue();
				
				if (config.hasClientBroadcast) {
					ConfigNetworkFunctions.clSendPlayerBroadcastConfigToServer(false, 
							configEntry.getKey(), config.clientConfig);
				}
				
				if (!ConfigNetworkFunctions.clientIsIntegratedServer()) {
					config.createBroadcastPlayersCfgHolder();
					config.createRemoteCommonCfgHolder();
				}
			});
		}
		
		@SubscribeEvent
		public static void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
			ALL_CONFIGS.values().forEach(config -> {
				config.serverBroadcast = null;
				config.commonConfigRemote = null;
			});
		}
		
		@SubscribeEvent
		public static void onServerStart(ServerStartingEvent event) {
			ALL_CONFIGS.values().forEach(config -> {
				config.createBroadcastPlayersCfgHolder();
			});
		}
		
		@SubscribeEvent
		public static void onServerStop(ServerStoppingEvent event) {
			ALL_CONFIGS.values().forEach(config -> {
				config.serverBroadcast = null;
			});
		}
	}
	

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.DEDICATED_SERVER)
	public static class ServerStartup {
		
		@SubscribeEvent
		public static void serverSetup(FMLDedicatedServerSetupEvent event) {
			ModRotpConfigEvent serverConfigsEvent = new ModRotpConfigEvent(Dist.DEDICATED_SERVER);
			ModLoader.postEvent(serverConfigsEvent);
			ALL_CONFIGS.putAll(serverConfigsEvent.configs);
			ALL_CONFIGS.entrySet().forEach(cfgEntry -> {
				ModConfig<?, ?, ?> config = cfgEntry.getValue();
				config.commonConfig.loadFromFileSystem();
			});
		}
	}

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ClientStartup {

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ModRotpConfigEvent clientConfigsEvent = new ModRotpConfigEvent(Dist.CLIENT);
			ModLoader.postEvent(clientConfigsEvent);
			ALL_CONFIGS.putAll(clientConfigsEvent.configs);
			ALL_CONFIGS.entrySet().forEach(cfgEntry -> {
				ModConfig<?, ?, ?> config = cfgEntry.getValue();
				config.commonConfig.loadFromFileSystem();
				config.clientConfig.loadFromFileSystem();
			});
		}
	}
}
