package com.github.standobyte.jojo.config.interfaces;

public interface ClientConfig<C1, C2> {
	C1 getLocalOnlyObj();
	C2 getBroadcastObj();

	boolean exists();
	boolean isDefault();
	void reset();
	
	void loadFromFileSystem();
	void saveToFileSystem();
	
	void sendBroadcastToServer(String configModId, boolean sendNonDefault);
}
