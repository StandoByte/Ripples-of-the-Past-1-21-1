package com.github.standobyte.jojo.config.client;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigTabType;
import com.mojang.datafixers.util.Either;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class RegisterRotpConfigScreenTabEvent extends Event implements IModBusEvent {
	public final Map<String, BiConsumer<ConfigTabType, ClientModSettingsScreen>> configGuiLayouts = new TreeMap<>();
	public final Map<String, Either<ResourceLocation, ItemStack>> configIcons = new TreeMap<>();

	public void register(String modId, BiConsumer<ConfigTabType, ClientModSettingsScreen> guiLayout) {
		this.configGuiLayouts.put(modId, guiLayout);
	}
	
	public void registerIcon(String modId, ResourceLocation iconPath) {
		this.configIcons.put(modId, Either.left(iconPath));
	}
	
	public void registerIconStand(String modId, ResourceLocation standId) {
		registerIcon(modId, standId.withPath(path -> "stand_skins/" + path + "/assets/" + standId.getNamespace() + "/textures/stand_icon.png"));
	}
	
	public void registerIcon(String modId, ItemStack item) {
		this.configIcons.put(modId, Either.right(item));
	}
	
}
