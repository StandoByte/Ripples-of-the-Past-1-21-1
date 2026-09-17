package com.github.standobyte.jojo.config.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.interfaces.ClientConfig;
import com.github.standobyte.jojo.config.interfaces.CommonConfig;
import com.github.standobyte.jojo.config.interfaces.ServerPlayerBroadcastConfig;
import com.github.standobyte.jojo.config.interfaces.fileimpl.ClientFileConfig;
import com.github.standobyte.jojo.config.interfaces.fileimpl.CommonFileConfig;
import com.github.standobyte.jojo.config.interfaces.fileimpl.ServerPlayerBroadcastObjectConfig;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

@ApiStatus.Internal
public class ModConfig<C1, C2, C3> implements ModConfigInterface<C1, C2, C3> {
	public final Dist currentEnvironment;
	public final String modId;
	public ClientConfig<C1, C2> clientConfig;
	@Nullable public Map<UUID, ServerPlayerBroadcastConfig<C2>> serverBroadcast = new HashMap<>();
	public CommonConfig<C3> commonConfig;
	protected CommonConfigState commonConfigState = CommonConfigState.LOCAL;
	
	public boolean hasClient;
	public boolean hasClientBroadcast;
	public boolean hasCommon;
	public Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory;
	
	public ModConfig(Dist currentEnvironment, String modId, 
			ClientConfig<C1, C2> clientConfig,
			CommonConfig<C3> commonConfig,
			Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory, 
			boolean hasClient, boolean hasClientBroadcast, boolean hasCommon) {
		this.currentEnvironment = currentEnvironment;
		this.modId = modId;
		this.clientConfig = clientConfig;
		this.commonConfig = commonConfig;
		this.hasClient = hasClient;
		this.hasClientBroadcast = hasClientBroadcast;
		this.hasCommon = hasCommon;
		this.broadcastFactory = broadcastFactory;
	}

	@Override
	public C1 getClient() {
		return clientConfig.getLocalOnlyObj();
	}

	@Override
	public C2 getPlayerBroadcast(@Nullable Player player) {
		if (player == null || player.isLocalPlayer()) {
			return clientConfig.getBroadcastObj();
		}
		ServerPlayerBroadcastConfig<C2> broadcast = getBroadcastPlayerState(player.getUUID());
		return broadcast != null ? broadcast.getObj() : null;
	}

	@Override
	public C3 getCommon() {
		return commonConfig.getObj();
	}

	@Override
	public void saveClient() {
		clientConfig.saveToFileSystem();
	}

	@Override
	public void sendClientBroadcast(boolean sendNonDefault) {
		clientConfig.sendBroadcastToServer(modId, sendNonDefault);
	}

	@Override
	public void saveCommon() {
		commonConfig.saveToFileSystem();
	}
	
	@Override
	public void sendCommonConfigEditToServer(ConfigOption<?> configOption) {
		commonConfig.sendEditToServer(modId, configOption);
	}
	
	@Override
	public String modId() {
		return modId;
	}
	
	
	public ServerPlayerBroadcastConfig<C2> getBroadcastPlayerState(UUID playerId) {
		if (serverBroadcast == null) return null;
		return serverBroadcast.computeIfAbsent(playerId, __ -> broadcastFactory.get());
	}
	
	public void createBroadcastPlayersCfgHolder() {
		if (this.hasClientBroadcast) {
			this.serverBroadcast = new HashMap<>();
		}
	}
	
	
	public enum CommonConfigState {
		LOCAL, // Our client / dedicated server 
		REMOTE // A remote server we're currently connected to
	}
	
	public void setCommonRemoteState() {
		if (this.hasCommon) {
			commonConfigState = CommonConfigState.REMOTE;
			commonConfig.setRemoteState();
		}
	}
	
	public void commonRemoteFromBuf(RegistryFriendlyByteBuf buf) {
		commonConfig.fromBuf(buf);
	}
	
	public void unsetCommonRemoteState() {
		if (this.hasCommon) {
			commonConfigState = CommonConfigState.LOCAL;
			commonConfig.unsetRemoteState();
		}
	}
	
	
	public static <C1, C2, C3> ModConfig<C1, C2, C3> modFileConfig(Dist currentEnvironment, 
			@Nullable Supplier<C1> client, 
			@Nullable Supplier<C2> clientBroadcast,
			@Nullable Supplier<C3> common,
			String modId) {
		boolean hasClient = client != null;
		boolean hasClientBroadcast = clientBroadcast != null;
		boolean hasCommon = common != null;
		if (client == null) client = () -> null;
		if (clientBroadcast == null) clientBroadcast = () -> null;
		if (common == null) common = () -> null;
		
		File mainDir = getMainDirectory(currentEnvironment);
		ClientConfig<C1, C2> clientConfig = switch (currentEnvironment) {
			case CLIENT -> new ClientFileConfig<>(
					ObjectConfigHelper.create(client.get()), 
					ObjectConfigHelper.create(clientBroadcast.get()), 
					mainDir, modId);
			case DEDICATED_SERVER -> null;
		};
		CommonConfig<C3> commonConfig = new CommonFileConfig<>(
				ObjectConfigHelper.create(common.get()), 
				mainDir, modId);
		final Supplier<C2> _clientBroadcast = clientBroadcast;
		Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory = () -> 
				new ServerPlayerBroadcastObjectConfig<>(ObjectConfigHelper.create(_clientBroadcast.get()));
		
		return new ModConfig<>(currentEnvironment, modId, 
				clientConfig, 
				commonConfig, 
				broadcastFactory, 
				hasClient, hasClientBroadcast, hasCommon);
	}
	
	
	@Nullable
	public static File getMainDirectory(Dist environment) {
		return switch (environment) {
			case CLIENT -> ClientProxy.getGameDirectory();
			case DEDICATED_SERVER -> null;
		};
	}
	
}
