package com.github.standobyte.jojo.adventure.character;

import net.minecraft.network.chat.Component;

public enum BloodType {
	A_FARMER("a"), 
	B_HUNTER("b"), 
	AB_DUALITY("ab"), 
	O_WARRIOR("o");

	public final Component name;
	
	BloodType(String name) {
		this.name = Component.translatable("jojo_ripples.blood_type." + name);
	}
}
