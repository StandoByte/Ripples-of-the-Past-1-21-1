package com.github.standobyte.jojo.powersystem.entityaction;

public enum ActionPhase {
	BUTTON_CHARGE,
	WINDUP,
	PERFORM,
	RECOVERY;
	
	public boolean isAtOfBefore(ActionPhase phase) {
		return this.ordinal() <= phase.ordinal();
	}
}
