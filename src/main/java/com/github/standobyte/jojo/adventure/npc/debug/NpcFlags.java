package com.github.standobyte.jojo.adventure.npc.debug;

/* When adding new flags, they should be phrased in such a way, 
 * so that the default value for NPCs actually being added in Surival gameplay is false
 */
public enum NpcFlags {
	AGGRESSIVE_TO_PLAYERS,
	NON_AGGRESSIVE_TO_MOBS,
	SHOW_HP,
	SHOW_HUNGER,
	SHOW_POWER_VARIABLES,
	DISABLE_AI_COMBAT,
	DISABLE_AI_EQUIPMENT,
	DISABLE_AI_EQUIP_CLOTHES,
	CAN_USE_STAND_DISC,
	CAN_TEST_ACTIONS,
	DISABLE_ARMOR_RENDER;
}
