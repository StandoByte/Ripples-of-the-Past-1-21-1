package com.github.standobyte.jojo.config.interfaces;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;

import net.minecraft.network.RegistryFriendlyByteBuf;

public interface CommonConfig<C3> {
	C3 getObj();

	boolean exists();
	boolean isDefault();
	void reset();
	
	void setRemoteState();
	void unsetRemoteState();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	
	void toBuf(RegistryFriendlyByteBuf buf);
	void fromBuf(RegistryFriendlyByteBuf buf);
	
	default void sendEditToServer(String modId, ConfigOption<?> configOption) {
		ConfigNetworkFunctions.clSendCommonSettingEditToServer(this, 
				modId, configOption);
	}
	ConfigOption<?> getConfigOptionByName(String name);
	String getConfigOptionName(ConfigOption<?> configOption);
	// kind of like a BiMap<String, ConfigOption<?>>
}
