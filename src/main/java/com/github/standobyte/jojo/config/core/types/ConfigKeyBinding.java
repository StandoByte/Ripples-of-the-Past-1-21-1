package com.github.standobyte.jojo.config.core.types;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ConfigKeyBinding extends ConfigOption<ConfigKeyBinding.KeyWithModifier> {
	public static record KeyWithModifier(Key key, KeyModifier modifier) {
		
		public KeyWithModifier(Key key) {
			this(key, KeyModifier.NONE);
		}
		
		public static final Codec<KeyWithModifier> JSON_CODEC = new Codec<>() {

			@Override
			public <T> DataResult<T> encode(KeyWithModifier input, DynamicOps<T> ops, T prefix) {
				String key = input.key.getName() + (input.modifier != KeyModifier.NONE ? ":" + input.modifier : "");
				return Codec.STRING.encode(key, ops, prefix);
			}

			@Override
			public <T> DataResult<Pair<KeyWithModifier, T>> decode(DynamicOps<T> ops, T input) {
				return Codec.STRING.decode(ops, input).map(result -> result.mapFirst(str -> {
					try {
						if (str.indexOf(':') != -1) {
							String[] pts = str.split(":");
							Key key = InputConstants.getKey(pts[0]);
							KeyModifier modifier = KeyModifier.valueFromString(pts[1]);
							return new KeyWithModifier(key, modifier);
						}
						else {
							Key key = InputConstants.getKey(str);
							return new KeyWithModifier(key);
						}
					}
					catch (IllegalArgumentException keyNotFound) {
						return new KeyWithModifier(InputConstants.UNKNOWN);
					}
				}));
			}
			
		};
	}
	
	private Supplier<KeyMapping> keybindSupplier;
	private KeyWithModifier loadedValue;
	public KeyMapping keybind;
	
	public ConfigKeyBinding(Supplier<KeyMapping> vanillaKeyMapping) {
		super(KeyWithModifier.JSON_CODEC, null);
		this.keybindSupplier = vanillaKeyMapping;
		__initWhenKeybindIsCreated.add(this);
	}

	@Override
	public KeyWithModifier get() {
		return new KeyWithModifier(keybind.getKey(), keybind.getKeyModifier());
	}

	@Override
	public void set(KeyWithModifier value) {
		set(value.key, value.modifier);
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
				keybind.setKeyModifierAndCode(loadedValue.modifier, loadedValue.key);
			}
			loadedValue = null;
			keybindSupplier = null;
		}
	}

}
