package com.github.standobyte.jojo.client.input.controlscheme;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.controls.InputBindTemplate;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class ClientInputBind {
	protected KeyMapping vanillaMapping;
	protected ClientKey immutableKey;
	@Nonnull protected KeyModifier immutableKeyModifier;
	
	public ClientInputBind(KeyMapping vanillaMapping) {
		this(vanillaMapping, null, null);
	}
	
	public ClientInputBind(ClientKey immutableKey) {
		this(null, immutableKey, KeyModifier.NONE);
	}
	
	public ClientInputBind(ClientKey immutableKey, KeyModifier modifier) {
		this(null, immutableKey, modifier);
	}
	
	@ApiStatus.Internal
	public ClientInputBind(KeyMapping vanillaMapping, ClientKey immutableKey, @Nonnull KeyModifier immutableKeyModifier) {
		this.vanillaMapping = vanillaMapping;
		this.immutableKey = immutableKey;
		this.immutableKeyModifier = immutableKeyModifier;
	}
	
	
	@Nullable
	public static ClientInputBind toClientInput(InputBindTemplate commonInput) {
		if (commonInput == null) return null;
		return switch (commonInput) {
			case InputKey hardcodedKey -> new ClientInputBind(toClientKey(hardcodedKey), toClientModifier(hardcodedKey.modifier));
			case InputUseVanillaMapping keyMapping -> {
				KeyMapping vanillaKeyMapping = keyMapping.toClientKeybind();
				yield vanillaKeyMapping != null ? new ClientInputBind(vanillaKeyMapping) : null;
			}
			default -> null;
		};
	}
	
    public static ClientKey toClientKey(InputKey key) {
    	return switch (key.device) {
    		case KEYBOARD -> ClientKey.make(InputConstants.Type.KEYSYM, key.keyCode);
    		case MOUSE -> ClientKey.make(InputConstants.Type.MOUSE, key.keyCode);
    	};
    }
    
    public static KeyModifier toClientModifier(InputKey.Modifier commonModifier) {
    	if (commonModifier == null) return KeyModifier.NONE;
    	return switch (commonModifier) {
    		case SHIFT -> KeyModifier.SHIFT;
    		case CONTROL -> KeyModifier.CONTROL;
    	};
    }
    
	
	@Nullable
	public ClientKey getKey() {
		if (vanillaMapping != null && !vanillaMapping.isUnbound()) {
			return ClientKey.fromVanillaKeybind(vanillaMapping);
		}
		if (immutableKey != null) {
			return immutableKey;
		}

		return null;
	}

	public InputConstants.Key getVanillaKey() {
		if (vanillaMapping != null && !vanillaMapping.isUnbound()) {
			return vanillaMapping.getKey();
		}
		if (immutableKey != null) {
			InputConstants.Key key = immutableKey.getVanillaKey();
			if (key != null) {
				return key;
			}
		}
		
		return InputConstants.UNKNOWN;
	}
	
	@Nonnull
	public KeyModifier getKeyModifier() {
		if (vanillaMapping != null && !vanillaMapping.isUnbound()) {
			return vanillaMapping.getKeyModifier();
		}
		if (immutableKeyModifier != null) {
			return immutableKeyModifier;
		}
		
		return KeyModifier.NONE;
	}
	
	public boolean keyMatches(ClientKey key, KeyModifier curModifier) {
		if (this.getKey() == key) {
			KeyModifier keyModifier = this.getKeyModifier();
			return keyModifier == KeyModifier.NONE || keyModifier == curModifier;
		}
		return false;
	}
	
}
