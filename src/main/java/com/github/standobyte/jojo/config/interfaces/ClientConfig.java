package com.github.standobyte.jojo.config.interfaces;

import com.github.standobyte.jojo.config.core.ConfigId;

public interface ClientConfig<C1, C2> extends ObjConfig {
	C1 getLocalOnlyObj();
	C2 getBroadcastObj();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	
	void sendBroadcastToServer(ConfigId configId, boolean sendNonDefault);
}
