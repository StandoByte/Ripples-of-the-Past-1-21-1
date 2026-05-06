package com.github.standobyte.jojo.client.standskin.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class LanguagePrep {
	public final Map<String, String> storage;
	public final Map<String, Component> componentStorage;

	public LanguagePrep(Map<String, String> storage, Map<String, Component> componentStorage) {
		this.storage = storage;
		this.componentStorage = componentStorage;
	}
	
	public static LanguagePrep fromJsonFile(List<Resource> resourceStack, String langCode, ResourceLocation skinId) {
		Map<String, String> storage = new HashMap<>();
		Map<String, Component> componentStorage = new HashMap<>();
		for (Resource resource : resourceStack) {
			try (InputStream inputStream = resource.open()) {
				Language.loadFromJson(inputStream, storage::put, componentStorage::put);
			} catch (IOException e) {
				JojoMod.getLogger().warn("Failed to load translations for {} for Stand skin {}", langCode, skinId, e);
			}
		}
		return new LanguagePrep(storage, componentStorage);
	}

}
