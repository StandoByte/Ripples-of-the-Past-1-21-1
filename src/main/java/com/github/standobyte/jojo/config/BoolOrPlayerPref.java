package com.github.standobyte.jojo.config;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;

public enum BoolOrPlayerPref {
	TRUE("options.on", Boolean.TRUE),
	FALSE("options.off", Boolean.FALSE),
	PLAYER_PREFERENCE("jojo_ripples.options.player_pref", null);

	public final Component optionStatus;
	private final String tlKey;
	@Nullable public final Boolean asBoolean;
	
	private BoolOrPlayerPref(String tlKey, Boolean asBoolean) {
		this.optionStatus = Component.translatable(tlKey);
		this.tlKey = tlKey + ".composed";
		this.asBoolean = asBoolean;
	}
	
	public Component optionStatus(Component optionName) {
		return Component.translatable(tlKey, optionName);
	}
	
}
