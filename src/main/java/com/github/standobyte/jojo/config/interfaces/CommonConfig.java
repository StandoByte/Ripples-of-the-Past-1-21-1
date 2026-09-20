package com.github.standobyte.jojo.config.interfaces;

import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

public interface CommonConfig<C3> {
	C3 getObj();

	boolean isDefault();
	void reset();
	
	void onServerSideApply(MinecraftServer server);
	
	void setRemoteState();
	void unsetRemoteState();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	void saveToFileSystem(ConfigOption<?> singleEditedOption);
	
	void toBuf(RegistryFriendlyByteBuf buf);
	void fromBuf(RegistryFriendlyByteBuf buf);
	
	default void sendEditToServer(ConfigId configId, ConfigOption<?> configOption) {
		ConfigNetworkFunctions.clSendCommonSettingEditToServer(this, 
				configId, configOption);
	}
	ConfigOption<?> getConfigOptionByName(String name);
	String getConfigOptionName(ConfigOption<?> configOption);
	// kind of like a BiMap<String, ConfigOption<?>>
}
