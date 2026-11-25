package com.github.standobyte.jojo.client.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.client.input.clickhold.AmbiguousKeyPress;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class VanillaKeyMappingExtended extends KeyMapping {
	public static final Set<String> ADD_DESC_TOOLTIP = new HashSet<>();
	public static final Map<String, ConfigBool> HOLD_OR_TOGGLE = new HashMap<>();
	
	protected static Map<String, MutableInt> PER_CATEGORY = new HashMap<String, MutableInt>();
	protected int orderIndex = Integer.MAX_VALUE;
	
	protected List<Consumer<AmbiguousKeyPress>> holdBehaviorForClickHoldSystem;

	public VanillaKeyMappingExtended(String name, int keyCode, String category) { super(name, keyCode, category); }
	public VanillaKeyMappingExtended(String name, InputConstants.Type type, int keyCode, String category) { super(name, type, keyCode, category); }
	public VanillaKeyMappingExtended(String description, IKeyConflictContext keyConflictContext, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, inputType, keyCode, category); }
	public VanillaKeyMappingExtended(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyCode, category); }
	public VanillaKeyMappingExtended(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, keyModifier, inputType, keyCode, category); }
	public VanillaKeyMappingExtended(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyModifier, keyCode, category); }
	
	public VanillaKeyMappingExtended inInitOrder() {
		this.orderIndex = PER_CATEGORY.computeIfAbsent(this.getCategory(), __ -> new MutableInt()).getAndIncrement();
		return this;
	}
	
	public VanillaKeyMappingExtended withDescTooltip() {
		ADD_DESC_TOOLTIP.add(this.getName());
		return this;
	}
	
	public VanillaKeyMappingExtended canBeHoldOrToggle(ConfigBool clientSetting) {
		HOLD_OR_TOGGLE.put(this.getName(), clientSetting);
		return this;
	}
	
	/**
	 * {@snippet : 
	 * @SubscribeEvent(priority = EventPriority.LOW)
	 * public static void afterKeybindsRegister(RegisterKeyMappingsEvent event) {
	 * 	VanillaKeyMappingExtended summonStandKey = InputHandler.getInstance().vanillaKeybinds.summonStand;
	 * 	summonStandKey.setUseClickHoldSystem(ambiguity -> {
	 * 		StandPower stand = ClientPowerCache.getPower(PowerClass.STAND);
	 * 		if (stand != null && stand.getPowerType() instanceof ActedStandType) {
	 * 			ambiguity.onHold = timeTook -> ActSelectionWheel.openWheel();
	 * 		}
	 * 	});
	 * }
	 * }
	 */
	public VanillaKeyMappingExtended setUseClickHoldSystem(Consumer<AmbiguousKeyPress> onHold) {
		if (holdBehaviorForClickHoldSystem == null) {
			holdBehaviorForClickHoldSystem = new ArrayList<>();
		}
		holdBehaviorForClickHoldSystem.add(onHold);
		return this;
	}



	@Override
	public int compareTo(KeyMapping other) {
		if (this.getCategory() == other.getCategory() && other instanceof VanillaKeyMappingExtended otherRegOrder) {
			int compare = Integer.compare(this.orderIndex, otherRegOrder.orderIndex);
			if (compare != 0) return compare;
		}
		return super.compareTo(other);
	}
	
	/** @return False if the increment should get canceled */
	public boolean onClickCountIncrement() {
		if (holdBehaviorForClickHoldSystem != null) {
			AmbiguousKeyPress ambiguousKeyPress = new AmbiguousKeyPress();
			ambiguousKeyPress.onClick = timeTook -> this.clickCount++;
			for (Consumer<AmbiguousKeyPress> behavior : holdBehaviorForClickHoldSystem) {
				behavior.accept(ambiguousKeyPress);
			}
			if (ambiguousKeyPress.onClick != null && ambiguousKeyPress.onHold != null) {
				ClientKey key = ClientKey.fromVanillaKeybind(this);
				HeldKeyTimer heldTimer = new HeldKeyTimer(key, true, getKeyModifier());
				InputHandler.getInstance().putHeldKeyTimer(key, heldTimer);
				heldTimer.ambiguousInputMethod = ambiguousKeyPress;
				return false;
			}
		}
		
		return true;
	}
}
