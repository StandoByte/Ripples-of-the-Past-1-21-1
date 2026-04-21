package com.github.standobyte.jojo.adventure.npc.debug;

/* When adding new flags, they should be phrased in such a way, 
 * so that the default value for NPCs actually being added in Surival gameplay is false
 */
public enum NpcFlags {
	DISABLE_HUNT_MONSTERS(true),
	DISABLE_AI_COMBAT(true),
	DISABLE_AI_EQUIPMENT(true),
	DISABLE_AI_CHANGE_CLOTHES(true), // TODO make the NPC pick clothes matching their moveset
	SHOW_HP(true),
	SHOW_HUNGER(true),
	SHOW_POWER_VARIABLES(true),
	DISABLE_RENDER_ARMOR(false);
	
	public final boolean trueForDebugDummy;
	
	private NpcFlags(boolean trueForDebugDummy) {
		this.trueForDebugDummy = trueForDebugDummy;
	}
}
