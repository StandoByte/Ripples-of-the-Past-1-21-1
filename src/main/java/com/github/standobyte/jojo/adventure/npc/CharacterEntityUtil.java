package com.github.standobyte.jojo.adventure.npc;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.subsystems.entity_playerwrapper.EntityAsPlayerWrapper;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CharacterEntityUtil {

	@Nullable
	public static Player getPlayerCastOrWrapped(LivingEntity playerOrNpc) {
		return switch (playerOrNpc) {
			case Player player -> player;
			case EntityAsPlayerWrapper npc -> npc.asPlayer();
			default -> null;
		};
	}
}
