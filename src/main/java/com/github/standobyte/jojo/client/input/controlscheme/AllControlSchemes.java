package com.github.standobyte.jojo.client.input.controlscheme;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.ability.controls.ControlSchemeTemplate;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class AllControlSchemes {
	public static Map<ResourceLocation, AbilityControlScheme> controls = new HashMap<>();
	
	public static AbilityControlScheme getForPowerType(PowerType powerType) {
		return controls.get(powerType.getId());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void createPowerControlSchemes(FMLClientSetupEvent event) {
		for (var playerPowerEntry : JojoRegistries.PLAYER_POWER_TYPES_REG.entrySet()) {
			add(playerPowerEntry.getValue());
		}
		
		// XXX (data-driven stands) load controls for data-driven stands
		for (var playerPowerEntry : JojoRegistries.DEFAULT_STANDS_REG.entrySet()) {
			add(playerPowerEntry.getValue());
		}
	}
	
	private static void add(PowerType powerType) {
		ResourceLocation id = powerType.getId();
		MovesetBuilder moveset = powerType.getDefaultMoveset();
		ControlSchemeTemplate defaultCtrlScheme = moveset.controlSchemes.values().iterator().next(); // the first control scheme that was created
		controls.put(id, AbilityControlScheme.create(defaultCtrlScheme, powerType));
	}
	
}
