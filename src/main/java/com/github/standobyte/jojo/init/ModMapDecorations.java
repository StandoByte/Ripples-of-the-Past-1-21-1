package com.github.standobyte.jojo.init;

import java.util.HashSet;
import java.util.Set;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModMapDecorations {
	public static final DeferredRegister<MapDecorationType> DECORATIONS = DeferredRegister.create(Registries.MAP_DECORATION_TYPE, JojoMod.MOD_ID);
	

	public static final Set<Holder<? extends MapDecorationType>> LARGE_ICONS = new HashSet<>();
	
	// TODO more map icons (both for specific dungeons and thematic)
	public static final Holder<MapDecorationType> HAMON_TEMPLE = DECORATIONS.register("hamon_temple", 
			id -> new MapDecorationType(id, true, 0x474747 /* gray for mountains */, true, false));

	public static final Holder<MapDecorationType> PILLAR_MAN_TEMPLE = DECORATIONS.register("pillar_man_temple", 
			id -> new MapDecorationType(id, true, 0x508d50 /* green for jungle */, true, false));

	public static final Holder<MapDecorationType> METEORITE = DECORATIONS.register("meteorite", 
			id -> new MapDecorationType(id, true, 0x6d6bb9 /* light blue for tundra */, true, false));

	// reminder that there's also MapDecorationType.NO_MAP_COLOR

	@SubscribeEvent
	public static void afterRegister(FMLCommonSetupEvent event) {
		LARGE_ICONS.add(PILLAR_MAN_TEMPLE);
	}
}
