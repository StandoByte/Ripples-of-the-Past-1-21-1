package com.github.standobyte.jojo.config.internal.cfgtypes;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;

public abstract class ClientConfig<C1, C2> {
	@Nullable public final ConfigObjSerialization<C1> localOnly;
	@Nullable public final ConfigObjSerialization<C2> broadcast;

	public ClientConfig(C1 localOnly, C2 broadcast) {
		this.localOnly = ConfigObjSerialization.create(localOnly);
		this.broadcast = ConfigObjSerialization.create(broadcast);
	}
	
	public boolean exists() {
		return localOnly != null || broadcast != null;
	}
	
	public void reset() {
		if (localOnly != null) {
			localOnly.reset();
		}
		if (broadcast != null) {
			broadcast.reset();
		}
	}

	public abstract void loadFromFileSystem();
	public abstract void saveToFileSystem();
	public abstract void sendToServer();

}
