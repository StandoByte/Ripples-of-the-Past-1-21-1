package com.github.standobyte.jojo.config.client;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigTabType;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterRotpConfigScreenTabEvent extends Event implements IModBusEvent {
	public final Map<String, BiConsumer<ConfigTabType, ClientModSettingsScreen>> configGuiLayouts = new TreeMap<>();

	public void register(String modId, BiConsumer<ConfigTabType, ClientModSettingsScreen> guiLayout) {
		this.configGuiLayouts.put(modId, guiLayout);
	}
	
}
