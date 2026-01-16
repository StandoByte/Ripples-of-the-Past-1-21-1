package com.github.standobyte.jojo.client.input;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.ability.Ability;

import net.minecraft.client.Minecraft;

public class ClickHoldResolve {
	public static final float timeIsHold = 4; // 200 ms
	public static final float timeAssumeHold = 2; // 100 ms
	private InputState curState = null;
	private float timeHeld;
	
	public final Ability clickBaseAbility;
	public final Ability heldBaseAbility;
	
	public ClickHoldResolve(Ability heldBaseAbility, Ability clickBaseAbility) {
		this.clickBaseAbility = clickBaseAbility;
		this.heldBaseAbility = heldBaseAbility;
	}
	
	@Nullable
	public Result frameUpdate(float tickDelta) {
		timeHeld += tickDelta;
		InputState newState = timeHeld < timeAssumeHold ? null : timeHeld < timeIsHold ? InputState.ASSUME_HOLD : InputState.HOLD;
		if (newState != curState) {
			this.curState = newState;
			float ticks = toGameTicks(timeHeld);
			return new Result(newState, ticks);
		}
		return null;
	}
	
	public Result keyReleased() {
		float ticks = toGameTicks(timeHeld);
		return new Result(timeHeld < timeIsHold ? InputState.CLICK : InputState.HOLD, ticks);
	}
	
	public static float toGameTicks(float frameTicks) {
		return frameTicks / 20 * Minecraft.getInstance().level.tickRateManager().tickrate();
	}
	
	
	public enum InputState {
		ASSUME_HOLD,
		HOLD,
		CLICK
	}
	
	public static record Result(InputState input, float timeTook) {}
}
