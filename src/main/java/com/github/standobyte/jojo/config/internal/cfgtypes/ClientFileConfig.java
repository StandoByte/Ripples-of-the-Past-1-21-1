package com.github.standobyte.jojo.config.internal.cfgtypes;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;

public class ClientFileConfig<C1, C2> {
	@Nullable public final ConfigObjSerialization<C1> localOnly;
	@Nullable public final ConfigObjSerialization<C2> broadcast;

	public ClientFileConfig(C1 localOnly, C2 broadcast, String modId) {
		this.localOnly = ConfigObjSerialization.create(localOnly);
		this.broadcast = ConfigObjSerialization.create(broadcast);
	}
	
	public void loadFromFileSystem() {
		if (localOnly != null || broadcast != null) {
			
		}
	}
	
	public void saveToFileSystem() {
		if (localOnly != null || broadcast != null) {
			
		}
	}
	
}
