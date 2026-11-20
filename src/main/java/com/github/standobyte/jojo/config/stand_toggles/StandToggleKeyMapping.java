package com.github.standobyte.jojo.config.stand_toggles;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.KeyCtxAndThen;
import com.github.standobyte.jojo.powersystem.PowerClass;
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
			return toggle != null && toggle.isAvailableAndActive(ClientPowerCache.getPower(PowerClass.STAND));
		}));
		this.getToggle = toggleField;
	}
	
	public ClientStandToggle getToggle() {
		if (toggle == null) {
			toggle = getToggle.get();
			if (toggle == null) {
				throw new IllegalStateException();
			}
		}
		return toggle;
	}

}
