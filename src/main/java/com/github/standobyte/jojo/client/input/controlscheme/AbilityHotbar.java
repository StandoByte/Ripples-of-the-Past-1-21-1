package com.github.standobyte.jojo.client.input.controlscheme;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.neoforged.neoforge.client.settings.KeyModifier;

public class AbilityHotbar {
	public ClientInputBind useAbilityKey;
	@Nullable public ClientInputBind switchAbilityKey;
	public List<AbilityHotbarSlot> slots = new ArrayList<>();
	public int slotIndex = 0;
	
	public AbilityHotbar(ClientInputBind useAbilityKey, @Nullable ClientInputBind switchAbilityKey) {
		this.useAbilityKey = useAbilityKey;
		this.switchAbilityKey = switchAbilityKey;
	}
	
	@Nullable
	public AbilityHotbarSlot getSelected() {
		return this.slotIndex >= 0 && this.slotIndex < this.slots.size() ? this.slots.get(this.slotIndex) : null;
	}
	
	public boolean alwaysSwitchAbility() {
		return switchAbilityKey == null || switchAbilityKey.getKey() == null;
	}
	
	public boolean isEmpty(KeyModifier curModifier) {
		return slots.stream().noneMatch(slot -> slot.showAbility(curModifier) != null);
	}
	
}
