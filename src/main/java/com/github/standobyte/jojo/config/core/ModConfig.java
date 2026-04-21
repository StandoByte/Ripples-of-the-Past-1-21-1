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
import com.github.standobyte.jojo.config.internal.cfgtypes.ClientFileConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.CommonConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.CommonFileConfig;
import com.github.standobyte.jojo.config.internal.cfgtypes.PlayerBroadcastConfig;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

@ApiStatus.Internal
public class ModConfig<C1, C2, C3> implements ModConfigInterface<C1, C2, C3> {
	public final Dist currentEnvironment;
	public final String modId;
	public ClientFileConfig<C1, C2> clientConfig;
	public Map<UUID, PlayerBroadcastConfig<C2>> serverBroadcast = new HashMap<>();
	public CommonFileConfig<C3> commonConfig;
	@Nullable public CommonConfig<C3> commonConfigRemote;

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
		return broadcast.broadcast.configObj;
	}

	@Override
	public C3 getCommon() {
		return getCommonCfgState().configState.configObj;
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
	
	
	public CommonConfig<C3> getCommonCfgState() {
		if (currentEnvironment == Dist.CLIENT && commonConfigRemote != null) {
			return commonConfigRemote;
		}
		return commonConfig;
	}
	
	public PlayerBroadcastConfig<C2> getBroadcastPlayerState(UUID playerId) {
		return serverBroadcast.computeIfAbsent(playerId, 
				__ -> new PlayerBroadcastConfig<>(broadcastFactory.get()));
	}
	
	public void createBroadcastPlayersCfgHolder() {
		if (this.hasClientBroadcast) {
			this.serverBroadcast = new HashMap<>();
		}
	}
	
	public void createRemoteCommonCfgHolder() {
		if (this.hasCommon) {
			this.commonConfigRemote = new CommonConfig<>(commonFactory.get());
		}
	}
	
	public boolean hasClient;
	public boolean hasClientBroadcast;
	public boolean hasCommon;
	public Supplier<C2> broadcastFactory;
	public Supplier<C3> commonFactory;
	
	
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
		this.commonConfig.loadFromFileSystem();
		this.broadcastFactory = clientBroadcast;
		switch (currentEnvironment) {
			case CLIENT -> {
				this.clientConfig = new ClientFileConfig<>(
						client.get(), 
						clientBroadcast.get(), 
						mainDir, modId);
				this.clientConfig.loadFromFileSystem();
				this.commonFactory = common;
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
