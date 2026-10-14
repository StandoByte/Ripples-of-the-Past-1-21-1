package com.github.standobyte.jojo.config.interfaces;

import java.util.Map;

import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

public interface CommonConfig<C3> extends ObjConfig {
	C3 getObj();
	
	boolean isDefault();
	void reset();
	
	void onServerSideApply(MinecraftServer server);
	
	void setRemoteState();
	void unsetRemoteState();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	void saveToFileSystem(Map<String, ConfigOption<?>> editedConfigOptions);
	
	void toBuf(RegistryFriendlyByteBuf buf);
	void fromBuf(RegistryFriendlyByteBuf buf);
	
	default void sendEditsToServer(ConfigId configId, Map<String, ConfigOption<?>> editedConfigOptions) {
		ConfigNetworkFunctions.clSendCommonSettingEditToServer(this, 
				configId, editedConfigOptions);
	}


	default ConfigOption<?> getConfigOptionByName(String name) {
		return getAllConfigOptions().get(name);
	}
	
	default String getConfigOptionName(ConfigOption<?> configOption) {
		return configOption.getFieldName();
	}
	// kind of like a BiMap<String, ConfigOption<?>>
}
