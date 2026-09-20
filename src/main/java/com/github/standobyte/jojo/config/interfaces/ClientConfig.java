package com.github.standobyte.jojo.config.interfaces;

import com.github.standobyte.jojo.config.core.ConfigId;

public interface ClientConfig<C1, C2> {
	C1 getLocalOnlyObj();
	C2 getBroadcastObj();

	boolean isDefault();
	void reset();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	
	void sendBroadcastToServer(ConfigId configId, boolean sendNonDefault);
}
