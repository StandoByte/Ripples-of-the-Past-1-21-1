package com.github.standobyte.jojo.adventure.npc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class LeFunnyNPCEasterEggs {

	public static void dropLootOnDeath(LivingEntity characterEntity, Player asPlayer, boolean dropAround, boolean includeThrowerName) {
		String name = characterEntity.getDisplayName().getString();
		if ("Notch".equals(name)) {
			asPlayer.drop(Items.APPLE.getDefaultInstance(), true, false);
		}
	}
	
}
