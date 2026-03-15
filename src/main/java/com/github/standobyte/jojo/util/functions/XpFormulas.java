package com.github.standobyte.jojo.util.functions;

import net.minecraft.world.entity.player.Player;

public class XpFormulas {
	
	public static int getTotalExperience(Player player) {
		int lvl = player.experienceLevel;
		int ptsForNextLvl = getXpNeededForNextLevel(lvl);
		int nextLvlProgressPts = (int) (player.experienceProgress * (float) ptsForNextLvl);
		int xp = getTotalXpOnLevel(lvl) + nextLvlProgressPts;
		return xp;
	}
	
	/** {@link Player#getXpNeededForNextLevel()} */
	public static int getXpNeededForNextLevel(int curLevel) {
		if (curLevel >= 30) 		return 112 + (curLevel - 30) * 9;
		else if (curLevel >= 15) 	return 37 + (curLevel - 15) * 5;
		else 						return 7 + curLevel * 2;
	}
	
	// https://minecraft.wiki/w/Experience#Leveling_up
	public static int getTotalXpOnLevel(int level) {
		if (level >= 32) 			return (int) (4.5 * level * level - 162.5 * level) + 2220;
		else if (level >= 17) 		return (int) (2.5 * level * level - 40.5 * level) + 360;
		else 						return level * level + 6 * level;
	}
}
