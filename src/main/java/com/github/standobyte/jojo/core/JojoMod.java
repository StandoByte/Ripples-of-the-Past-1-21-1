package com.github.standobyte.jojo.core;

import org.slf4j.Logger;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.command.argument.ModCommandArguments;
import com.github.standobyte.jojo.config.ModConfigInterface;
import com.github.standobyte.jojo.config.RotpConfig;
import com.github.standobyte.jojo.config.core.RegisterRotpConfigEvent;
import com.github.standobyte.jojo.init.ModBlockEntities;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.init.ModEntityCustomEffects;
import com.github.standobyte.jojo.init.ModEntityDataSerializers;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.init.power.ModStands;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

// TODO (!) reorganize the packages after merging the two branches (+ don't forget to update the readme)
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
		
		ModStandAbilities.ABILITY_TYPES.register(modEventBus);
		ModEntityCustomEffects.CUSTOM_EFFECTS.register(modEventBus);
		ModPlayerPowers.PLAYER_POWERS.register(modEventBus);
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
	
	
	public static ModConfigInterface<RotpConfig.Client, RotpConfig.ClientBroadcast, RotpConfig.Common> config;
	
	@SubscribeEvent
	public void registerConfig(RegisterRotpConfigEvent event) {
		switch (event.environment) {
			case CLIENT -> {
				config = event.registerConfig(RotpConfig.ID, 
						RotpConfig.Client::new,
						RotpConfig.ClientBroadcast::new, 
						RotpConfig.Common::new);
			}
			case DEDICATED_SERVER -> {
				/* Because in our case the client config contains objects of classes that references client-only vanilla classes
				 * (ClientConfigKeyBinding has KeyMapping field, which is client only),
				 * we have to register the config this way, so that the dedicated server doesn't try to load the Client class 
				 * (which would lead to a crash).
				 */
				config = event.registerConfig(RotpConfig.ID, 
						null,
						RotpConfig.ClientBroadcast::new, 
						RotpConfig.Common::new);
			}
		}
	}

}
