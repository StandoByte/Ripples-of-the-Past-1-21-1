package com.github.standobyte.jojo.client.standskin.text;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.chat.Component;

public class StandNameSetColor {

	public static Component fromSkin(Power<?> maybeStandPower, Component standName, boolean whiteBackground) {
		if (maybeStandPower instanceof StandPower standPower) {
			StandInstance standInstance = standPower.getStandInstance().orElse(null);
			if (standInstance != null) {
				standName = fromSkin(standInstance, standName, whiteBackground);
			}
		}
		return standName;
	}

	public static Component fromSkin(StandInstance standInstance, Component standName, boolean whiteBackground) {
		StandSkin skin = StandSkinsLoader.getInstance().getSkin(standInstance);
		if (skin != null) {
			var colors = skin.getColors();
			int color = whiteBackground ? colors.text_white_bg() : colors.text();
			return standName.copy().withColor(color);
		}
		return standName;
	}
}
