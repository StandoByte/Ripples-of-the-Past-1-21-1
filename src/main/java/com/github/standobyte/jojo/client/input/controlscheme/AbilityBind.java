package com.github.standobyte.jojo.client.input.controlscheme;

import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;

public class AbilityBind {
	public ClientInputBind input;
	public InputMethod inputMethod;
	public AbilityControlsEntry ability;
	
	public AbilityBind(ClientInputBind input, InputMethod inputMethod, AbilityControlsEntry ability) {
		this.input = input;
		this.inputMethod = inputMethod;
		this.ability = ability;
	}
}
