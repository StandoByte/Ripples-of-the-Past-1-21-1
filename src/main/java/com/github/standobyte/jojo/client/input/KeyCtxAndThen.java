package com.github.standobyte.jojo.client.input;

import java.util.function.BooleanSupplier;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class KeyCtxAndThen implements IKeyConflictContext {
	public BooleanSupplier extraCondition;

	public KeyCtxAndThen(BooleanSupplier extraCondition) {
		this.extraCondition = extraCondition;
	}
	
	@Override
	public boolean isActive() {
		return KeyConflictContext.IN_GAME.isActive() && extraCondition.getAsBoolean();
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		return KeyConflictContext.IN_GAME.conflicts(other);
	}
}
