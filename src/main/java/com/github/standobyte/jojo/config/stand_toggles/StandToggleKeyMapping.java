package com.github.standobyte.jojo.config.stand_toggles;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.input.KeyCtxAndThen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class StandToggleKeyMapping extends KeyMapping {
	protected Supplier<ClientStandToggle> getToggle;
	protected ClientStandToggle toggle;

	public StandToggleKeyMapping(String description, Supplier<ClientStandToggle> toggleField, 
			String category) {
		this(description, toggleField, InputConstants.UNKNOWN, category);
	}

	public StandToggleKeyMapping(String description, Supplier<ClientStandToggle> toggleField, 
			Key defaultKey, String category) {
		super(description, KeyConflictContext.IN_GAME, defaultKey, category);
		setKeyConflictContext(new KeyCtxAndThen(() -> {
			ClientStandToggle toggle = getToggle();
			return toggle != null && toggle.activeWhen.getAsBoolean();
		}));
		this.getToggle = toggleField;
	}
	
	public ClientStandToggle getToggle() {
		if (toggle == null) {
			ClientStandToggles.lazyInitToggles();
			toggle = getToggle.get();
		}
		return toggle;
	}

}
