package com.github.standobyte.jojo.adventure.npc;

import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.entity.XpOrbTargetingEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CharacterModEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void attractXpToCharacter(XpOrbTargetingEvent event) {
		PowerUserMobEntity.attractXpToCharacter(event);
	}

}
