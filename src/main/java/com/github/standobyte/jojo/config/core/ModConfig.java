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
	public final ConfigId configId;
	@Nullable public ClientConfig<C1, C2> clientConfig;
	@Nullable public CommonConfig<C3> commonConfig;
	@Nullable public Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory;
	
	protected CommonConfigState commonConfigState = CommonConfigState.LOCAL;
	@Nullable protected Map<UUID, ServerPlayerBroadcastConfig<C2>> serverBroadcast = new HashMap<>();
	
	public ModConfig(Dist currentEnvironment, ConfigId configId, 
			@Nullable ClientConfig<C1, C2> clientConfig,
			@Nullable CommonConfig<C3> commonConfig,
			@Nullable Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory) {
		this.currentEnvironment = currentEnvironment;
		this.configId = configId;
		this.clientConfig = clientConfig;
		this.commonConfig = commonConfig;
		this.broadcastFactory = broadcastFactory;
	}
	
	public boolean hasClient() {
		return clientConfig != null;
	}
	
	public boolean hasCommon() {
		return commonConfig != null;
	}
	
	public boolean hasClientBroadcast() {
		return broadcastFactory != null;
	}

	@Override
	public C1 getClient() {
		if (!hasClient()) return null;
		
		return clientConfig.getLocalOnlyObj();
	}

	@Override
	public C2 getPlayerBroadcast(@Nullable Player player) {
		if (!hasClientBroadcast()) return null;
		
		if (player == null || player.isLocalPlayer()) {
			return clientConfig.getBroadcastObj();
		}
		ServerPlayerBroadcastConfig<C2> broadcast = getBroadcastPlayerState(player.getUUID());
		return broadcast != null ? broadcast.getObj() : null;
	}

	@Override
	public C3 getCommon() {
		if (!hasCommon()) return null;
		
		return commonConfig.getObj();
	}

	@Override
	public void saveClient() {
		if (hasClient()) clientConfig.saveToFileSystem();
	}

	@Override
	public void sendClientBroadcast(boolean sendNonDefault) {
		if (hasClientBroadcast()) clientConfig.sendBroadcastToServer(configId, sendNonDefault);
	}

	@Override
	public void saveCommon() {
		if (hasCommon()) commonConfig.saveToFileSystem();
	}

	@Override
	public void saveCommon(ConfigOption<?> singleEditedOption) {
		if (hasCommon()) commonConfig.saveToFileSystem(singleEditedOption);
	}
	
	@Override
	public void sendCommonConfigEditToServer(ConfigOption<?> configOption) {
		if (hasCommon()) commonConfig.sendEditToServer(configId, configOption);
	}
	
	@Override
	public ConfigId configId() {
		return configId;
	}
	
	
	public void createBroadcastPlayersCfgHolder() {
		if (this.hasClientBroadcast()) {
			this.serverBroadcast = new HashMap<>();
		}
	}
	
	public ServerPlayerBroadcastConfig<C2> getBroadcastPlayerState(UUID playerId) {
		if (serverBroadcast == null) return null;
		return serverBroadcast.computeIfAbsent(playerId, __ -> broadcastFactory.get());
	}
	
	public boolean removeBroadcastPlayerState(UUID playerId) {
		return serverBroadcast != null && serverBroadcast.remove(playerId) != null;
	}
	
	public void destroyBroadcastPlayersCfgHolder() {
		this.serverBroadcast = null;
	}
	
	
	public enum CommonConfigState {
		LOCAL, // Our client / dedicated server 
		REMOTE // A remote server we're currently connected to
	}
	
	public void setCommonRemoteState() {
		if (this.hasCommon()) {
			commonConfigState = CommonConfigState.REMOTE;
			commonConfig.setRemoteState();
		}
	}
	
	public void commonRemoteFromBuf(RegistryFriendlyByteBuf buf) {
		if (this.hasCommon()) {
			commonConfig.fromBuf(buf);
		}
	}
	
	public void unsetCommonRemoteState() {
		if (this.hasCommon()) {
			commonConfigState = CommonConfigState.LOCAL;
			commonConfig.unsetRemoteState();
		}
	}
	
	
	public static <C1, C2, C3> ModConfig<C1, C2, C3> modFileConfig(Dist currentEnvironment, 
			@Nullable Supplier<C1> client, 
			@Nullable Supplier<C2> clientBroadcast,
			@Nullable Supplier<C3> common,
			String modId) {
		ConfigId configId = new ConfigId(ConfigId.ConfigClass.MOD, modId);
		File mainDir = getMainDirectory(currentEnvironment);
		ClientConfig<C1, C2> clientConfig = null;
		CommonConfig<C3> commonConfig = null;
		Supplier<ServerPlayerBroadcastConfig<C2>> broadcastFactory = null;
		
		if (currentEnvironment == Dist.CLIENT && (client != null || clientBroadcast != null)) {
			clientConfig = new ClientFileConfig<>(
					client != null ? ObjectConfigHelper.create(client.get()) : null, 
					clientBroadcast != null ? ObjectConfigHelper.create(clientBroadcast.get()) : null, 
					mainDir, modId);
		}
		
		if (common != null) {
			commonConfig = new CommonFileConfig<>(
					ObjectConfigHelper.create(common.get()), 
					mainDir, modId);
		}
		
		if (clientBroadcast != null) {
			broadcastFactory = () -> new ServerPlayerBroadcastObjectConfig<>(
					ObjectConfigHelper.create(clientBroadcast.get()));
		}
		
		return new ModConfig<>(currentEnvironment, configId, 
				clientConfig, 
				commonConfig, 
				broadcastFactory);
	}
	
	
	@Nullable
	public static File getMainDirectory(Dist environment) {
		return switch (environment) {
			case CLIENT -> ClientProxy.getGameDirectory();
			case DEDICATED_SERVER -> null;
		};
	}
	
}
