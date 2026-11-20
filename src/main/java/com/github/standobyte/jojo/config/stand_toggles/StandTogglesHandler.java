package com.github.standobyte.jojo.config.stand_toggles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class StandTogglesHandler {
	public static List<ClientStandToggle> toggles = new ArrayList<>(3);
	
	public static void addToggle(ClientStandToggle toggle) {
		toggles.add(toggle);
	}
	
	public static Iterable<ClientStandToggle> get() {
		return toggles;
	}
	
	public static void onTickPost(Minecraft mc) {
		for (ClientStandToggle toggle : toggles) {
			if (toggle.keybind != null) {
				KeyMapping keybind = toggle.keybind.get();
				if (keybind != null) {
					while (keybind.consumeClick()) {
						if (toggle.clientCanToggle()) {
							toggle.toggle();
							toggle.fadeOut.reset();
						}
						else {
							mc.gui.setOverlayMessage(Component.translatable("jojo_ripples.toggle_overruled", 
									toggle.overrulingCommonSetting.get().optionStatus), false);
						}
					}
				}
			}
		}
	}
}
