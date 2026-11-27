package com.github.standobyte.jojo.config.internal;

import static com.github.standobyte.jojo.config.internal.ConfigEventHandler.getConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.interfaces.CommonConfig;
import com.github.standobyte.jojo.config.interfaces.ServerPlayerBroadcastConfig;
import com.github.standobyte.jojo.config.internal.packets.ClCommonServerConfigEditPacket;
import com.github.standobyte.jojo.config.internal.packets.ClCommonServerConfigResetPacket;
import com.github.standobyte.jojo.config.internal.packets.ClPlayerBroadcastConfigPacket;
import com.github.standobyte.jojo.config.internal.packets.PlayerBroadcastConfigPacket;
import com.github.standobyte.jojo.config.internal.packets.RemoteCommonConfigPacket;
import com.github.standobyte.jojo.config.internal.packets.RemoteHostUUIDConfigPacket;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
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
	@Nullable public static UUID hostOfARemoteServerTheClientIsCurrentlyConnectedTo;
	
	public static boolean clientIsConnectedToAServer() {
		return Minecraft.getInstance().getConnection() != null;
	}
	
	public static void sendServerHostUUID(ServerPlayer loggingIn) {
		UUID lanHostUUID = getIntegratedServerHostUUID(loggingIn.server);
		PacketDistributor.sendToPlayer(loggingIn, new RemoteHostUUIDConfigPacket(Optional.ofNullable(lanHostUUID)));
	}
	
	public static void clAcceptServerHostUUID(RemoteHostUUIDConfigPacket payload) {
		hostOfARemoteServerTheClientIsCurrentlyConnectedTo = payload.hostUUID.orElse(null);
	}
	
	public static void clClearServerHostUUID() {
		hostOfARemoteServerTheClientIsCurrentlyConnectedTo = null;
	}

	public static boolean clientHasPermissions() {
		if (clientIsIntegratedServer()) {
			return true;
		}
		
		Player player = ClientProxy.getClientPlayer();
		return player != null
				&& hostOfARemoteServerTheClientIsCurrentlyConnectedTo == null
				&& player.hasPermissions(CAN_USE_GAMERULES_AT_LEVEL);
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
		Minecraft mc = Minecraft.getInstance();
		if (mc.isLocalServer()) {
			IntegratedServer server = mc.getSingleplayerServer();
			return server != null;
		}
		return false;
	}
	
	@Nullable
	public static UUID getIntegratedServerHostUUID(MinecraftServer server) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.isLocalServer()) {
				IntegratedServer integratedServer = mc.getSingleplayerServer();
				if (integratedServer != null && integratedServer == server) {
					// FIXME the local player object has not been created (and therefore the uuid is still null) by the time PlayerEvent.PlayerLoggedInEvent is called (ConfigNetworkFunctions.sendServerHostUUID(ServerPlayer))
					/* Though funnily enough it still ends up working correctly - 
					 * the local server host will receive a packet with empty UUID optional 
					 * and set hostOfARemoteServerTheClientIsCurrentlyConnectedTo to null, 
					 * which is exactly the state that the host is supposed to be in.
					 */
					UUID hostUUID = integratedServer.uuid;
					return hostUUID;
				}
			}
		}
		return null;
	}
	
	public static boolean playerIsIntegratedServerHost(ServerPlayer player) {
		UUID hostUUID = getIntegratedServerHostUUID(player.server);
		return playerUUIDMatches(player, hostUUID);
	}
	
	private static boolean playerUUIDMatches(ServerPlayer player, @Nullable UUID hostIdIfIntegratedServer) {
		return hostIdIfIntegratedServer != null && player.getUUID().equals(hostIdIfIntegratedServer);
	}
	
	
	
	public static void srvSendConfigsToLoggedInPlayer(ServerPlayer loggingIn, ConfigId configId, ModConfig<?, ?, ?> config) {
		if (config.hasCommon()) {
			CommonConfig<?> commonConfig = config.commonConfig;
			if (!commonConfig.isDefault()) {
				RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
						configId, commonConfig::toBuf);
				PacketDistributor.sendToPlayer(loggingIn, commonPacket);
			}
		}

		if (config.hasClientBroadcast()) {
			for (ServerPlayer player : loggingIn.server.getPlayerList().getPlayers()) {
				UUID playerId = player.getUUID();
				ServerPlayerBroadcastConfig<?> broadcastConfig = config.getBroadcastPlayerState(playerId);
				if (!broadcastConfig.isDefault()) {
					PlayerBroadcastConfigPacket playerBroadcastPacket = new PlayerBroadcastConfigPacket(
							playerId, configId, broadcastConfig::toBuf);
					if (player != loggingIn) {
						PacketDistributor.sendToPlayer(loggingIn, playerBroadcastPacket);
					}
				}
			}
		}
	}
	
	public static void srvSendCommonConfigStateToAllPlayers(MinecraftServer server, ConfigId configId, CommonConfig<?> commonConfig) {
		RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
				configId, commonConfig::toBuf);
		UUID integratedHostUUID = getIntegratedServerHostUUID(server);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!playerUUIDMatches(player, integratedHostUUID)) {
				PacketDistributor.sendToPlayer(player, commonPacket);
			}
		}
	}
	
	public static void clAcceptCommonConfigState(RemoteCommonConfigPacket payload) {
		ConfigId configId = payload.configId;
		ModConfig<?, ?, ?> config = getConfig(configId);
		config.commonRemoteFromBuf(payload.read);
	}
	
	
	
	public static void clSendPlayerBroadcastConfigToServer(ConfigId configId, Consumer<RegistryFriendlyByteBuf> write) {
		if (ConfigNetworkFunctions.clientIsConnectedToAServer()) {
			ClPlayerBroadcastConfigPacket broadcastPacket = new ClPlayerBroadcastConfigPacket(
					configId, write);
			PacketDistributor.sendToServer(broadcastPacket);
		}
	}
	
	public static void srvAcceptPlayerBroadcastConfig(ServerPlayer player, ClPlayerBroadcastConfigPacket payload) {
		UUID playerId = player.getUUID();
		ConfigId configId = payload.configId;
		ModConfig<?, ?, ?> config = getConfig(configId);
		ServerPlayerBroadcastConfig<?> broadcast = config.getBroadcastPlayerState(playerId);
		broadcast.fromBuf(payload.read, player);

		PlayerBroadcastConfigPacket playerBroadcastPacket = new PlayerBroadcastConfigPacket(
				playerId, configId, broadcast::toBuf);
		UUID integratedHostUUID = getIntegratedServerHostUUID(player.server);
		for (ServerPlayer otherPlayer : player.server.getPlayerList().getPlayers()) {
			if (otherPlayer != player && !playerUUIDMatches(otherPlayer, integratedHostUUID)) {
				PacketDistributor.sendToPlayer(otherPlayer, playerBroadcastPacket);
			}
		}
	}
	
	public static void srvResetPlayerBroadcastConfigForAllPlayers(MinecraftServer server, ServerPlayer player, ConfigId configId) {
		PlayerBroadcastConfigPacket playerBroadcastPacket = PlayerBroadcastConfigPacket.reset(
				player.getUUID(), configId);
		UUID integratedHostUUID = getIntegratedServerHostUUID(server);
		for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
			if (otherPlayer != player && !playerUUIDMatches(otherPlayer, integratedHostUUID)) {
				PacketDistributor.sendToPlayer(otherPlayer, playerBroadcastPacket);
			}
		}
	}
	
	public static void clAcceptAnotherPlayerBroadcastConfig(PlayerBroadcastConfigPacket payload) {
		UUID playerId = payload.playerId;
		ConfigId configId = payload.configId;
		ModConfig<?, ?, ?> config = getConfig(configId);
		ServerPlayerBroadcastConfig<?> broadcast = config.getBroadcastPlayerState(playerId);
		Player remotePlayer = Minecraft.getInstance().level.getPlayerByUUID(playerId);
		boolean reset = payload.read.isEmpty();
		if (reset) {
			broadcast.reset(remotePlayer);
		}
		else {
			broadcast.fromBuf(payload.read.get(), remotePlayer);
		}
	}
	
	
	
	public static void clSendCommonSettingEditToServer(CommonConfig<?> commonConfig, 
			ConfigId configId, Map<String, ConfigOption<?>> editedConfigOptions) {
		if (clientHasPermissions() && !editedConfigOptions.isEmpty()) {
			ClCommonServerConfigEditPacket packet = new ClCommonServerConfigEditPacket(
					configId, editedConfigOptions);
			PacketDistributor.sendToServer(packet);
		}
	}
	
	public static void srvAcceptCommonSettingEdit(ServerPlayer sender, ConfigId configId, RegistryFriendlyByteBuf buf, Collection<String> fieldNames) {
		MinecraftServer server = sender.level().getServer();
		if (playerHasPermissions(sender)) {
			ModConfig<?, ?, ?> config = getConfig(configId);
			if (!config.hasCommon()) return;
			
			CommonConfig<?> commonConfig = config.commonConfig;
			Map<String, ConfigOption<?>> editedConfigOptions = new HashMap<>();
			
			for (String fieldName : fieldNames) {
				ConfigOption<?> field = commonConfig.getConfigOptionByName(fieldName);
				if (field == null) {
					JojoMod.getLogger().error("Field {} not found in {} config {}", fieldName, configId.configClass(), configId.modOrStandId());
					continue;
				}
				
				field.fromBufWithCallback(buf, null);
				editedConfigOptions.put(fieldName, field);
				if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
					CommandSourceStack srcStack = sender.createCommandSourceStack();
					srcStack.sendSuccess(() -> Component.translatable("jojo_ripples.config_common.set", 
							configId.getConfigTitle(), 
							fieldName, 
							field.get().toString()), true);
				}
			}
			
			config.commonConfig.onServerSideApply(server);
			config.saveCommon(editedConfigOptions);
			
			RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
					configId, commonConfig::toBuf);
			UUID integratedHostUUID = getIntegratedServerHostUUID(server);
			for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
				if (!playerUUIDMatches(player, integratedHostUUID)) {
					PacketDistributor.sendToPlayer(player, commonPacket);
				}
			}
		}
		else {
			sender.connection.disconnect(Component.literal("Unauthorized attempt of ROTP config editing"));
		}
	}
	
	public static void clSendCommonConfigResetToServer(ConfigId configId) {
		if (clientHasPermissions()) {
			ClCommonServerConfigResetPacket packet = new ClCommonServerConfigResetPacket(configId);
			PacketDistributor.sendToServer(packet);
		}
	}
	
	public static void srvAcceptCommonSettingsReset(ServerPlayer sender, ClCommonServerConfigResetPacket payload) {
		MinecraftServer server = sender.level().getServer();
		if (playerHasPermissions(sender)) {
			ConfigId configId = payload.configId;
			ModConfig<?, ?, ?> config = getConfig(configId);
			if (!config.hasCommon()) return;
			
			CommonConfig<?> commonConfig = config.commonConfig;
			commonConfig.reset();
			commonConfig.onServerSideApply(server);
			
			if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
				CommandSourceStack srcStack = sender.createCommandSourceStack();
				srcStack.sendSuccess(() -> Component.translatable("jojo_ripples.config_common.reset", 
						configId.getConfigTitle()), true);
			}
			
			commonConfig.saveToFileSystem();
			
			RemoteCommonConfigPacket commonPacket = new RemoteCommonConfigPacket(
					configId, commonConfig::toBuf);
			UUID integratedHostUUID = getIntegratedServerHostUUID(server);
			for (ServerPlayer player : sender.server.getPlayerList().getPlayers()) {
				if (!playerUUIDMatches(player, integratedHostUUID)) {
					PacketDistributor.sendToPlayer(player, commonPacket);
				}
			}
		}
		else {
			sender.connection.disconnect(Component.literal("Unauthorized attempt of ROTP config editing"));
		}
	}
	
}
