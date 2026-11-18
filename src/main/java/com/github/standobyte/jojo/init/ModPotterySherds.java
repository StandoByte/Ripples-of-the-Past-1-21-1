package com.github.standobyte.jojo.init;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModPotterySherds {
	public static final DeferredRegister<DecoratedPotPattern> POTTERY_SHERDS = DeferredRegister.create(Registries.DECORATED_POT_PATTERN, JojoMod.MOD_ID);

	public static final Holder<DecoratedPotPattern> PATTERN_TRIBE = POTTERY_SHERDS.register("tribe", id -> new DecoratedPotPattern(id));
	public static final Holder<DecoratedPotPattern> PATTERN_SUN = POTTERY_SHERDS.register("sun", id -> new DecoratedPotPattern(id));
	public static final Holder<DecoratedPotPattern> PATTERN_MASK = POTTERY_SHERDS.register("mask", id -> new DecoratedPotPattern(id));
	public static final Holder<DecoratedPotPattern> PATTERN_ULTIMATE = POTTERY_SHERDS.register("ultimate", id -> new DecoratedPotPattern(id));
	
	public static final Map<Item, ResourceKey<DecoratedPotPattern>> MOD_ITEM_TO_POT_TEXTURE = new HashMap<>();
	
	
	@SubscribeEvent
	public static void tiePotDecorToSherdItems(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			MOD_ITEM_TO_POT_TEXTURE.put(ModItems.POTTERY_SHERD_PILLAR_MAN_1.get(), PATTERN_TRIBE.getKey());
			MOD_ITEM_TO_POT_TEXTURE.put(ModItems.POTTERY_SHERD_PILLAR_MAN_2.get(), PATTERN_SUN.getKey());
			MOD_ITEM_TO_POT_TEXTURE.put(ModItems.POTTERY_SHERD_PILLAR_MAN_3.get(), PATTERN_MASK.getKey());
			MOD_ITEM_TO_POT_TEXTURE.put(ModItems.POTTERY_SHERD_PILLAR_MAN_4.get(), PATTERN_ULTIMATE.getKey());
		});
	}
}
