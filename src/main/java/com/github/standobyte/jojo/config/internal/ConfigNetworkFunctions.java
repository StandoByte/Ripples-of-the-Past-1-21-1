package com.github.standobyte.jojo.config.internal;

import java.util.UUID;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.interfaces.CommonConfig;
import com.github.standobyte.jojo.config.interfaces.ServerPlayerBroadcastConfig;
import com.github.standobyte.jojo.config.internal.packets.ClCommonServerConfigEditPacket;
import com.github.standobyte.jojo.config.internal.packets.ClCommonServerConfigResetPacket;
import com.github.standobyte.jojo.config.internal.packets.ClPlayerBroadcastConfigPacket;
import com.github.standobyte.jojo.config.internal.packets.PlayerBroadcastConfigPacket;
import com.github.standobyte.jojo.config.internal.packets.RemoteCommonConfigPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

public class ConfigNetworkFunctions {
	
	public static boolean clientIsConnectedToAServer() {
		return Minecraft.getInstance().getConnection() != null;
	}

	public static boolean clientHasPermissions() {
		if (clientIsIntegratedServer()) {
			return true;
		}
		
		Player player = ClientProxy.getClientPlayer();
		return player != null && player.hasPermissions(CAN_USE_GAMERULES_AT_LEVEL);
	}
	
	public static int CAN_USE_GAMERULES_AT_LEVEL = 2;
	public static boolean playerHasPermissions(ServerPlayer player) {
		MinecraftServer server = player.server;
		boolean isDedicatedServer = server.isDedicatedServer();
		if (isDedicatedServer) {
			return player.hasPermissions(CAN_USE_GAMERULES_AT_LEVEL);
		}
		else {
			return playerIsIntegratedServerHost(player);
		}
	}
	
	public static boolean clientIsIntegratedServer() {
		return Minecraft.getInstance().hasSingleplayerServer();
	}
	
	public static boolean playerIsIntegratedServerHost(ServerPlayer player) {
		return FMLEnvironment.dist == Dist.CLIENT && clientIsIntegratedServer();
	}
	
	
	
	public static void srvSendConfigsToLoggedInPlayer(ServerPlayer loggingIn, String configModId, ModConfig<?, ?, ?> config) {
		if (!playerIsIntegratedServerHost(loggingIn)) {
			if (config.hasCommon) {
				CommonConfig<?> commonConfig = config.commonConfig;
				if (commonConfig.exists() && !commonConfig.isDefault()) {
					RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
							configModId, commonConfig::toBuf);
					PacketDistributor.sendToPlayer(loggingIn, commonPacket);
				}
			}

			if (config.hasClientBroadcast) {
				for (ServerPlayer player : loggingIn.server.getPlayerList().getPlayers()) {
					UUID playerId = player.getUUID();
					ServerPlayerBroadcastConfig<?> broadcastConfig = config.getBroadcastPlayerState(playerId);
					if (!broadcastConfig.isDefault()) {
						PlayerBroadcastConfigPacket playerBroadcastPacket = new PlayerBroadcastConfigPacket(
								playerId, configModId, broadcastConfig::toBuf);
						if (player != loggingIn) {
							PacketDistributor.sendToPlayer(loggingIn, playerBroadcastPacket);
						}
					}
				}
			}
		}
	}
	
	public static void srvSendCommonConfigStateToAllPlayers(MinecraftServer server, String configModId, CommonConfig<?> commonConfig) {
		if (commonConfig.exists()) {
			RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
					configModId, commonConfig::toBuf);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!playerIsIntegratedServerHost(player)) {
					PacketDistributor.sendToPlayer(player, commonPacket);
				}
			}
		}
	}
	
	public static void clAcceptCommonConfigState(RemoteCommonConfigPacket payload) {
		String configModId = payload.modId;
		ModConfig<?, ?, ?> config = getConfig(configModId);
		config.commonRemoteFromBuf(payload.read);
	}
	
	
	
	public static void clSendPlayerBroadcastConfigToServer(String configModId, Consumer<RegistryFriendlyByteBuf> write) {
		if (ConfigNetworkFunctions.clientIsConnectedToAServer()) {
			ClPlayerBroadcastConfigPacket broadcastPacket = new ClPlayerBroadcastConfigPacket(
					configModId, write);
			PacketDistributor.sendToServer(broadcastPacket);
		}
	}
	
	public static void srvAcceptPlayerBroadcastConfig(ServerPlayer player, ClPlayerBroadcastConfigPacket payload) {
		UUID playerId = player.getUUID();
		String configModId = payload.modId;
		ModConfig<?, ?, ?> config = getConfig(configModId);
		ServerPlayerBroadcastConfig<?> broadcast = config.getBroadcastPlayerState(playerId);
		broadcast.fromBuf(payload.read);

		PlayerBroadcastConfigPacket playerBroadcastPacket = new PlayerBroadcastConfigPacket(
				playerId, configModId, broadcast::toBuf);
		for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
			if (otherPlayer != player && !playerIsIntegratedServerHost(otherPlayer)) {
				PacketDistributor.sendToPlayer(otherPlayer, playerBroadcastPacket);
			}
		}
	}
	
	public static void srvResetPlayerBroadcastConfigForAllPlayers(MinecraftServer server, ServerPlayer player, String modId) {
		PlayerBroadcastConfigPacket playerBroadcastPacket = PlayerBroadcastConfigPacket.reset(
				player.getUUID(), modId);
		for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
			if (otherPlayer != player && !playerIsIntegratedServerHost(otherPlayer)) {
				PacketDistributor.sendToPlayer(otherPlayer, playerBroadcastPacket);
			}
		}
	}
	
	public static void clAcceptAnotherPlayerBroadcastConfig(PlayerBroadcastConfigPacket payload) {
		UUID playerId = payload.playerId;
		String configModId = payload.modId;
		ModConfig<?, ?, ?> config = getConfig(configModId);
		ServerPlayerBroadcastConfig<?> broadcast = config.getBroadcastPlayerState(playerId);
		boolean reset = payload.read.isEmpty();
		if (reset) {
			broadcast.reset();
		}
		else {
			broadcast.fromBuf(payload.read.get());
		}
	}
	
	
	
	public static void clSendCommonSettingEditToServer(CommonConfig<?> commonConfig, 
			String configModId, ConfigOption<?> field) {
		if (clientHasPermissions()) {
			String fieldName = commonConfig.getConfigOptionName(field);
			ClCommonServerConfigEditPacket packet = new ClCommonServerConfigEditPacket(
					configModId, fieldName, field::toBuf);
			PacketDistributor.sendToServer(packet);
		}
	}
	
	public static void srvAcceptCommonSettingEdit(ServerPlayer sender, ClCommonServerConfigEditPacket payload) {
		MinecraftServer server = sender.level().getServer();
		if (playerHasPermissions(sender)) {
			String configModId = payload.modId;
			String fieldName = payload.fieldName;
			ModConfig<?, ?, ?> config = getConfig(configModId);
			CommonConfig<?> commonConfig = config.commonConfig;
			ConfigOption<?> field = commonConfig.getConfigOptionByName(fieldName);
			field.fromBuf(payload.read);
			
			if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
				CommandSourceStack srcStack = sender.createCommandSourceStack();
				srcStack.sendSuccess(() -> Component.translatable("jojo_ripples.config_common.set", 
						Component.translatable(configModId + ".config.title"), 
						fieldName, 
						field.get().toString()), true);
			}
			
			commonConfig.saveToFileSystem();
			
			RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
					configModId, commonConfig::toBuf);
			for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
				if (!playerIsIntegratedServerHost(player)) {
					PacketDistributor.sendToPlayer(player, commonPacket);
				}
			}
			
		}
		else {
			sender.connection.disconnect(Component.literal("Unauthorized attempt of ROTP config editing"));
		}
	}
	
	public static void clSendCommonConfigResetToServer(String configModId) {
		if (clientHasPermissions()) {
			ClCommonServerConfigResetPacket packet = new ClCommonServerConfigResetPacket(
					configModId);
			PacketDistributor.sendToServer(packet);
		}
	}
	
	public static void srvAcceptCommonSettingsReset(ServerPlayer sender, ClCommonServerConfigResetPacket payload) {
		MinecraftServer server = sender.level().getServer();
		if (playerHasPermissions(sender)) {
			String configModId = payload.modId;
			ModConfig<?, ?, ?> config = getConfig(configModId);	
			CommonConfig<?> commonConfig = config.commonConfig;
			commonConfig.reset();
			
			if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
				CommandSourceStack srcStack = sender.createCommandSourceStack();
				srcStack.sendSuccess(() -> Component.translatable("jojo_ripples.config_common.reset", 
						Component.translatable(configModId + ".config.title")), true);
			}
			
			commonConfig.saveToFileSystem();
			
			RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
					configModId, commonConfig::toBuf);
			for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
				if (!playerIsIntegratedServerHost(player)) {
					PacketDistributor.sendToPlayer(player, commonPacket);
				}
			}
			
		}
		else {
			sender.connection.disconnect(Component.literal("Unauthorized attempt of ROTP config editing"));
		}
	}
	
	public static ModConfig<?, ?, ?> getConfig(String configModId) {
		return ConfigEventHandler.ALL_CONFIGS.get(configModId);
	}
	
}
