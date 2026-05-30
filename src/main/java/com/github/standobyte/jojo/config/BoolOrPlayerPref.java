package com.github.standobyte.jojo.config;

import net.minecraft.network.chat.Component;

public enum BoolOrPlayerPref {
	TRUE("options.on.composed"),
	FALSE("options.off.composed"),
	PLAYER_PREFERENCE("jojo_ripples.options.player_pref.composed");
	
	private final String tlKey;
	
	private BoolOrPlayerPref(String tlKey) {
		this.tlKey = tlKey;
	}
	
	public Component optionStatus(Component optionName) {
		return Component.translatable(tlKey, optionName);
	}
	
}
