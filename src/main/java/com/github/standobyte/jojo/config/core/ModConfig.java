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
import com.github.standobyte.jojo.config.internal.cfgtypes.ClientConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.ClientFileConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.CommonConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.CommonFileConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.PlayerBroadcastConfig;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

@ApiStatus.Internal
public class ModConfig<C1, C2, C3> implements ModConfigInterface<C1, C2, C3> {
	public final Dist currentEnvironment;
	public final String modId;
	public ClientConfig<C1, C2> clientConfig;
	@Nullable public Map<UUID, PlayerBroadcastConfig<C2>> serverBroadcast = new HashMap<>();
	public CommonConfig<C3> commonConfig;
	protected CommonConfigState commonConfigState = CommonConfigState.LOCAL;

	@Override
	public C1 getClient() {
		return clientConfig.localOnly.configObj;
	}

	@Override
	public C2 getPlayerBroadcast(@Nullable Player player) {
		if (player == null || player.isLocalPlayer()) {
			return clientConfig.broadcast.configObj;
		}
		PlayerBroadcastConfig<C2> broadcast = getBroadcastPlayerState(player.getUUID());
		return broadcast != null ? broadcast.broadcast.configObj : null;
	}

	@Override
	public C3 getCommon() {
		return commonConfig.configState.configObj;
	}

	@Override
	public void saveClient() {
		clientConfig.saveToFileSystem();
	}

	@Override
	public void sendClientBroadcast() {
		clientConfig.sendToServer();
	}

	@Override
	public void saveCommon() {
		commonConfig.saveToFileSystem();
	}
	
	@Override
	public String modId() {
		return modId;
	}
	
	
	public PlayerBroadcastConfig<C2> getBroadcastPlayerState(UUID playerId) {
		if (serverBroadcast == null) return null;
		return serverBroadcast.computeIfAbsent(playerId, 
				__ -> new PlayerBroadcastConfig<>(broadcastFactory.get()));
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
	
	public void createCommonRemote() {
		if (this.hasCommon) {
			commonConfigState = CommonConfigState.REMOTE;
			commonConfig.configState.configOptions.values().forEach(ConfigOption::setRemoteConfig);
		}
	}
	
	public void commonRemoteFromBuf(RegistryFriendlyByteBuf buf) {
		commonConfig.configState.fromBuf(buf);
	}
	
	public void removeCommonRemote() {
		if (this.hasCommon) {
			commonConfigState = CommonConfigState.LOCAL;
			commonConfig.configState.configOptions.values().forEach(ConfigOption::unsetRemoteConfig);
		}
	}
	
	
	public boolean hasClient;
	public boolean hasClientBroadcast;
	public boolean hasCommon;
	public Supplier<C2> broadcastFactory;
	
	
	public ModConfig(Dist currentEnvironment, 
			@Nullable Supplier<C1> client, 
			@Nullable Supplier<C2> clientBroadcast,
			@Nullable Supplier<C3> common,
			String modId) {
		this.currentEnvironment = currentEnvironment;
		this.modId = modId;
		this.hasClient = client != null;
		this.hasClientBroadcast = clientBroadcast != null;
		this.hasCommon = common != null;
		if (client == null) client = () -> null;
		if (clientBroadcast == null) clientBroadcast = () -> null;
		if (common == null) common = () -> null;
		
		File mainDir = getMainDirectory(currentEnvironment);
		this.commonConfig = new CommonFileConfig<>(common.get(), mainDir, modId);
		this.broadcastFactory = clientBroadcast;
		switch (currentEnvironment) {
			case CLIENT -> {
				this.clientConfig = new ClientFileConfig<>(
						client.get(), 
						clientBroadcast.get(), 
						mainDir, modId);
			}
			case DEDICATED_SERVER -> {}
		}
	}
	
	
	@Nullable
	public static File getMainDirectory(Dist environment) {
		return switch (environment) {
			case CLIENT -> ClientProxy.getGameDirectory();
			case DEDICATED_SERVER -> null;
		};
	}
	
}
