package com.github.standobyte.jojo.config;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;

public enum BoolOrPlayerPref {
	TRUE("options.on.composed", Boolean.TRUE),
	FALSE("options.off.composed", Boolean.FALSE),
	PLAYER_PREFERENCE("jojo_ripples.options.player_pref.composed", null);
	
	private final String tlKey;
	@Nullable public final Boolean asBoolean;
	
	private BoolOrPlayerPref(String tlKey, Boolean asBoolean) {
		this.tlKey = tlKey;
		this.asBoolean = asBoolean;
	}
	
	public Component optionStatus(Component optionName) {
		return Component.translatable(tlKey, optionName);
	}
	
}
