package com.github.standobyte.jojo.config.internal.cfgtypes;

import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;

public class PlayerBroadcastConfig<C2> {
	public final ConfigObjSerialization<C2> broadcast;

	public PlayerBroadcastConfig(C2 broadcast) {
		this.broadcast = ConfigObjSerialization.create(broadcast);
	}
	
}
