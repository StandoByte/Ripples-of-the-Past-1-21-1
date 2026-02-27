package com.github.standobyte.jojo.powersystem.ability.controls;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.util.reflection.ClientReflection;

import net.minecraft.client.KeyMapping;

public class InputUseVanillaMapping implements InputBindTemplate {
	public String keyMappingName;

	public InputUseVanillaMapping(KeyMapping keyBind) {
		this(keyBind.getName());
	}
	
	public InputUseVanillaMapping(String keyName) {
		this.keyMappingName = keyName;
	}
    
    @Nullable
    public KeyMapping toClientKeybind() {
    	return ClientReflection.getKeyMappingMapByName().get(this.keyMappingName);
    }
}
