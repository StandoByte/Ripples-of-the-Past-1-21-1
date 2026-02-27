package com.github.standobyte.jojo.client.input;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.clickhold.AmbiguousKeyPress;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class HeldKeyTimer {
	public final ClientKey key;
	public final boolean cancelVanilla;
	public final KeyModifier modifier;
	protected float timeHeld;
	@Nullable protected AmbiguousKeyPress ambiguousInputMethod;
	
	public HeldKeyTimer(ClientKey key, boolean cancelVanilla, KeyModifier modifier) {
		this.key = key;
		this.cancelVanilla = cancelVanilla;
		this.modifier = modifier;
		this.timeHeld = 0;
	}
	
	
	@Nullable
	public AmbiguousKeyPress.Result frameUpdate(float tickDelta) {
		timeHeld += tickDelta;
		AmbiguousKeyPress ambiguousClickHold = getAmbiguousInputMethod();
		if (ambiguousClickHold != null) {
			return ambiguousClickHold.frameUpdate(tickDelta);
		}
		return null;
	}
	
	public float getRealTimeTicks() {
		return timeHeld;
	}


	public void setAmbiguousInputMethod(@Nullable AmbiguousKeyPress inputMethod) {
		this.ambiguousInputMethod = inputMethod;
	}
	
	@Nullable
	public AmbiguousKeyPress getAmbiguousInputMethod() {
		return this.ambiguousInputMethod;
	}
	
	public boolean isDefinitelyHold() {
		return this.ambiguousInputMethod == null;
	}
	
}
