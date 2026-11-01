package com.github.standobyte.jojo.config;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum BoolOrPlayerPref {
	TRUE(Component.translatable("options.on").withStyle(ChatFormatting.GREEN), Boolean.TRUE),
	FALSE(Component.translatable("options.off").withStyle(ChatFormatting.RED), Boolean.FALSE),
	PLAYER_PREFERENCE(Component.translatable("jojo_ripples.options.player_pref"), null);

	public final Component optionStatus;
	@Nullable public final Boolean asBoolean;
	
	private BoolOrPlayerPref(Component optionStatus, Boolean asBoolean) {
		this.optionStatus = optionStatus;
		this.asBoolean = asBoolean;
	}
	
	public MutableComponent optionStatus(Component optionName) {
		return Component.translatable("options.generic_value", optionName, optionStatus);
	}

	public static Component optionStatus(boolean value) {
		return (value ? BoolOrPlayerPref.TRUE : BoolOrPlayerPref.FALSE).optionStatus;
	}

	public static MutableComponent optionStatus(Component optionName, boolean value) {
		return (value ? BoolOrPlayerPref.TRUE : BoolOrPlayerPref.FALSE).optionStatus(optionName);
	}

}
