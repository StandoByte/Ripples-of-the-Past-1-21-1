package com.github.standobyte.jojo.config.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.input.SerializeKeybind;
import com.github.standobyte.jojo.client.input.SerializeKeybind.KeyWithModifier;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ConfigKeyBinding extends ConfigOption<KeyWithModifier> {
	private Supplier<KeyMapping> keybindSupplier;
	private KeyWithModifier loadedValue;
	public KeyMapping keybind;
	
	public ConfigKeyBinding(Supplier<KeyMapping> vanillaKeyMapping) {
		super(SerializeKeybind.CODEC, null);
		this.keybindSupplier = vanillaKeyMapping;
		__initWhenKeybindIsCreated.add(this);
	}

	@Override
	public KeyWithModifier get() {
		return new KeyWithModifier(keybind.getKey(), keybind.getKeyModifier());
	}

	@Override
	public void set(KeyWithModifier value) {
		set(value.key(), value.modifier());
	}
	
	public void set(Key key, KeyModifier modifier) {
		if (keybind != null) {
			keybind.setKeyModifierAndCode(modifier, key);
		}
		else {
			loadedValue = new KeyWithModifier(key, modifier);
		}
	}

	@Override
	public boolean isDefault() {
		return keybind.isDefault();
	}

	@Override
	public void reset() {
		if (keybind != null) {
			keybind.setKeyModifierAndCode(keybind.getDefaultKeyModifier(), keybind.getDefaultKey());
		}
		else {
			loadedValue = null;
		}
	}
	
	
	static List<ConfigKeyBinding> __initWhenKeybindIsCreated = new ArrayList<>();
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void afterKeybindsAreCreated(RegisterKeyMappingsEvent event) {
		for (ConfigKeyBinding config : __initWhenKeybindIsCreated) {
			config.resolveKeybind();
		}
		__initWhenKeybindIsCreated = null;
	}
	
	public void resolveKeybind() {
		if (keybind == null && keybindSupplier != null) {
			keybind = keybindSupplier.get();
			if (keybind != null && loadedValue != null) {
				keybind.setKeyModifierAndCode(loadedValue.modifier(), loadedValue.key());
			}
			loadedValue = null;
			keybindSupplier = null;
		}
	}

}
