package com.github.standobyte.jojo.config.client;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigType;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterRotpConfigScreenTabEvent extends Event implements IModBusEvent {
	public final Map<String, BiConsumer<ConfigType, ClientModSettingsScreen>> configGuiLayouts = new TreeMap<>();

	public void register(String modId, BiConsumer<ConfigType, ClientModSettingsScreen> guiLayout) {
		this.configGuiLayouts.put(modId, guiLayout);
	}
	
}
