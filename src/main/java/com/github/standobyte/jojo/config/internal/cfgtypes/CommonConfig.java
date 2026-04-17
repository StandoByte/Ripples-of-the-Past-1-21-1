package com.github.standobyte.jojo.config.internal.cfgtypes;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;

public class CommonConfig<C3> {
	@Nullable public final ConfigObjSerialization<C3> configState;

	public CommonConfig(C3 remoteState) {
		this.configState = ConfigObjSerialization.create(remoteState);
	}
	
}
