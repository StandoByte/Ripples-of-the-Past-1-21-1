package com.github.standobyte.jojo.powersystem.playerpower;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;

public abstract class PlayerPowerData extends PowerData {
	public abstract PlayerPowerType<?> getType();
	
	@Override public PowerClass<?> getPowerClass() { return PowerClass.PLAYER_POWER; }
}
