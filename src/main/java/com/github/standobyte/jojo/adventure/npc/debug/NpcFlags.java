package com.github.standobyte.jojo.adventure.npc.debug;

/* When adding new flags, they should be phrased in such a way, 
 * so that the default value for NPCs actually being added in Surival gameplay is false
 */
public enum NpcFlags {
	AGGRESSIVE_TO_PLAYERS(false),
	NON_AGGRESSIVE_TO_MOBS(true),
	SHOW_HP(true),
	SHOW_HUNGER(true),
	SHOW_POWER_VARIABLES(true),
	DISABLE_AI_COMBAT(true),
	DISABLE_AI_EQUIPMENT(true),
	DISABLE_AI_EQUIP_CLOTHES(true),
	CAN_USE_STAND_DISC(true),
	CAN_TEST_ACTIONS(true),
	DISABLE_ARMOR_RENDER(false);
	
	public final boolean trueForDebugDummy;
	
	private NpcFlags(boolean trueForDebugDummy) {
		this.trueForDebugDummy = trueForDebugDummy;
	}
}
