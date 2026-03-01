package com.github.standobyte.jojo.core;

import org.slf4j.Logger;

import com.github.standobyte.jojo.core.command.argument.ModCommandArguments;
import com.github.standobyte.jojo.init.ModBlockEntities;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.core.ModContainers;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.init.core.ModEntityDataSerializers;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.init.power.ModStandEffects;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojoimpl.powers.hamon.ModHamonSkills;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// TODO (!) reorganize the packages after merging the two branches
// XXX allow PowerType to override controls/HUD rendering
@Mod(JojoMod.MOD_ID)
public class JojoMod {
	public static final String MOD_ID = "jojo_ripples";
	@Deprecated
	public static final Logger LOGGER = LogUtils.getLogger();

	public JojoMod(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.register(this);

		ModBlocks.BLOCKS.register(modEventBus);
		ModItems.ITEMS.register(modEventBus);
		ModItems.CREATIVE_MODE_TABS.register(modEventBus);
		ModEntityTypes.ENTITY_TYPES.register(modEventBus);
		ModDataAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
		ModCommandArguments.ARGUMENT_TYPES.register(modEventBus);
		ModSoundEvents.SOUNDS.register(modEventBus);
		ModParticles.PARTICLES.register(modEventBus);
		ModItemDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
		ModEntityAttributes.ATTRIBUTES.register(modEventBus);
		ModEntityDataSerializers.SERIALIZERS.register(modEventBus);
		ModStatusEffects.STATUS_EFFECTS.register(modEventBus);
		ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
		ModContainers.CONTAINERS.register(modEventBus);
		
		JojoRegistries.ABILITY_TYPES.register(modEventBus);
		ModPlayerPowers.PLAYER_POWERS.register(modEventBus);
		ModHamonSkills.HAMON_SKILLS.register(modEventBus);
		ModHamonSkills.HAMON_CHARACTER_TECHNIQUES.register(modEventBus);
		ModStandAbilities.STAND_EFFECT_TYPES.register(modEventBus);
		ModStandEffects.load();
		ModStands.DEFAULT_STANDS.register(modEventBus);
		ModSpecialActions.ACTIONS.register(modEventBus);
	}
	
	public static ResourceLocation resLoc(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	@SubscribeEvent
	private void commonSetup(FMLCommonSetupEvent event) {
	}
	
	@SubscribeEvent
	private void registerNetwork(RegisterPayloadHandlersEvent event) {
		PacketsRegister.register(event);
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
	
	
	@Deprecated
	public static boolean disableDevStuff() {
		return FMLLoader.isProduction();
	}

}
