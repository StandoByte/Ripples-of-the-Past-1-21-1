package com.github.standobyte.jojo.config.internal;

import java.util.UUID;

import com.github.standobyte.jojo.client.ui.screen_widgets.utils.ButtonDecoration;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen;
import com.github.standobyte.jojo.config.client.RegisterRotpConfigScreenTabEvent;
import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.core.RegisterRotpConfigEvent;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class ConfigEventHandler {
	public static ConfigsMap ALL_CONFIGS;
	
	
	@EventBusSubscriber(modid = JojoMod.MOD_ID)
	public static class EventHandler {
		
		@SubscribeEvent
		public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			ALL_CONFIGS.allConfigs().forEach(config -> {
				ConfigNetworkFunctions.srvSendConfigsToLoggedInPlayer(player, 
						config.configId(), config);
			});
		}
		
		@SubscribeEvent
		public static void onServerPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			MinecraftServer server = player.serverLevel().getServer();
			UUID playerId = player.getUUID();
			ALL_CONFIGS.allConfigs().forEach(config -> {
				if (config.removeBroadcastPlayerState(playerId)) {
					ConfigNetworkFunctions.srvResetPlayerBroadcastConfigForAllPlayers(
							server, player, config.configId());
				}
			});
		}
		
		@SubscribeEvent
		public static void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
			ALL_CONFIGS.allConfigs().forEach(config -> {
				if (config.hasClientBroadcast()) {
					config.sendClientBroadcast(false);
				}
				
				if (!ConfigNetworkFunctions.clientIsIntegratedServer()) {
					config.createBroadcastPlayersCfgHolder();
					config.setCommonRemoteState();
				}
			});
		}
		
		@SubscribeEvent
		public static void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
			ALL_CONFIGS.allConfigs().forEach(config -> {
				config.destroyBroadcastPlayersCfgHolder();
				config.unsetCommonRemoteState();
			});
		}
		
		@SubscribeEvent
		public static void onServerStart(ServerStartingEvent event) {
			ALL_CONFIGS.allConfigs().forEach(config -> {
				config.createBroadcastPlayersCfgHolder();
			});
		}
		
		@SubscribeEvent
		public static void onServerStop(ServerStoppingEvent event) {
			ALL_CONFIGS.allConfigs().forEach(config -> {
				config.destroyBroadcastPlayersCfgHolder();
			});
		}
	}
	

	// TODO "loading" and "reloading" events?
	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.DEDICATED_SERVER)
	public static class ServerStartup {
		
		@SubscribeEvent
		public static void serverSetup(FMLDedicatedServerSetupEvent event) {
			final Dist dist = Dist.DEDICATED_SERVER;
			ALL_CONFIGS = new ConfigsMap(dist);
			RegisterRotpConfigEvent serverConfigsEvent = new RegisterRotpConfigEvent(dist);
			ModLoader.postEvent(serverConfigsEvent);
			for (ModConfig<?, ?, ?> config : serverConfigsEvent.configs.values()) {
				ALL_CONFIGS.add(config);
			}
			ALL_CONFIGS.allConfigs().forEach(config -> {
				if (config.hasCommon()) config.commonConfig.loadFromFileSystem();
			});
		}
	}

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ClientStartup {

		@SubscribeEvent
		public static void veryEarlyClientSetup(RegisterClientReloadListenersEvent event) {
			final Dist dist = Dist.CLIENT;
			ALL_CONFIGS = new ConfigsMap(dist);
			RegisterRotpConfigEvent clientConfigsEvent = new RegisterRotpConfigEvent(dist);
			ModLoader.postEvent(clientConfigsEvent);
			for (ModConfig<?, ?, ?> config : clientConfigsEvent.configs.values()) {
				ALL_CONFIGS.add(config);
			}
			ALL_CONFIGS.allConfigs().forEach(config -> {
				if (config.hasCommon()) config.commonConfig.loadFromFileSystem();
				if (config.hasClient()) config.clientConfig.loadFromFileSystem();
			});
		}

		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			RegisterRotpConfigScreenTabEvent registerLayoutEvent = new RegisterRotpConfigScreenTabEvent();
			ModLoader.postEvent(registerLayoutEvent);
			ClientModSettingsScreen.CONFIG_GUI_LAYOUTS.putAll(registerLayoutEvent.configGuiLayouts);
			
			for (var iconEntry : registerLayoutEvent.configIcons.entrySet()) {
				ClientModSettingsScreen.CONFIG_TAB_ICONS.put(iconEntry.getKey(), iconEntry.getValue()
						.map(icon -> ButtonDecoration.icon(new GuiIcon(icon, 16, 16)), ButtonDecoration::item));
			}
		}
	}
	
	public static ModConfig<?, ?, ?> getConfig(ConfigId configId) {
		return ConfigEventHandler.ALL_CONFIGS.get(configId);
	}
}
