package com.github.standobyte.jojo.config;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.core.ConfigOption;

import net.minecraft.world.entity.player.Player;

public interface ModConfigInterface<C1, C2, C3> {
	C1 getClient();
	C2 getPlayerBroadcast(@Nullable Player player);
	C3 getCommon();
	
	void saveClient();
	void sendClientBroadcast(boolean sendNonDefault);
	void saveCommon();
	void saveCommon(Map<String, ConfigOption<?>> editedConfigOptions);
	void sendCommonConfigEditsToServer(Map<String, ConfigOption<?>> editedConfigOptions);
	
	ConfigId configId();
}
