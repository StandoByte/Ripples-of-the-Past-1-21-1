package com.github.standobyte.jojo.powersystem.playerpower;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;

public abstract class PlayerPowerData extends PowerData {
	
	public PlayerPowerData(PlayerPowerType<?> powerType) {
		super(powerType);
	}
	
	@Override
	public PlayerPowerType<?> getPowerType() {
		return (PlayerPowerType<?>) super.getPowerType();
	}
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.PLAYER_POWER; }
}
