package com.github.standobyte.jojo.client.input;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;

import net.neoforged.neoforge.common.util.TriState;

public class AbilityInputState {

	public static AbilityInputState init() {
		return withValue(1 << IS_ACTIVE);
	}

	public void setFlag(int flag, boolean value) {
		this._value = _setBit(this._value, flag, value);
	}

	public boolean getFlag(int flag) {
		return _getBit(this._value, flag);
	}

	// named flags

	public static final int IS_ACTIVE = 0;
	public static final int VISIBLE_WHEN_INACTIVE = 1;

	public static final int ONLY_IN_CONTAINER = 2;
	public static final int WITH_ITEM_HELD = 3;
	public static final int HIGH_PRIORITY = 4;

	
	public static boolean isInputActive(AbilityInputState state, boolean inContainerMenu) {
		return state.getFlag(IS_ACTIVE) && !(state.getFlag(AbilityInputState.ONLY_IN_CONTAINER) && !inContainerMenu);
	}
	
	public static boolean showAbilityInHUD(AbilityInputState state, TriState forContainerMenu) {
		boolean showAbility = state.getFlag(AbilityInputState.IS_ACTIVE)
				|| state.getFlag(AbilityInputState.VISIBLE_WHEN_INACTIVE);
		showAbility &= state.getFlag(AbilityInputState.ONLY_IN_CONTAINER) == forContainerMenu.isTrue();
		return showAbility;
	}
	
	public static boolean showAbilityInHUD(AbilityConditionCheck ability, TriState forContainerMenu) {
		AbilityInputState state = AbilityInputState.withValue(ability.clientInputState);
		return AbilityInputState.showAbilityInHUD(state, forContainerMenu);
	}


	@ApiStatus.Internal protected static AbilityInputState instance = new AbilityInputState();
	@ApiStatus.Internal protected AbilityInputState() {}

	@ApiStatus.Internal public int _value = 1;

	@ApiStatus.Internal public static AbilityInputState withValue(int value) {
		instance._value = value;
		return instance;
	}

	@ApiStatus.Internal public static int _setBit(int num, int bit, boolean value) {
		if (value)	return num | (1 << bit);
		else 		return num & ~(1 << bit);
	}

	@ApiStatus.Internal public static boolean _getBit(int num, int bit) {
		return (num & (1 << bit)) > 0;
	}
}
