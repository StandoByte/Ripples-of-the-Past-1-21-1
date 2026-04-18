package com.github.standobyte.jojo.config;

import com.github.standobyte.jojo.config.client.ClientModSettingsScreen;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigTabType;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.client.RegisterRotpConfigScreenTabEvent;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.config.core.types.ConfigFloat;
import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

public class RotpConfig {
	public static final String ID = JojoMod.MOD_ID;

	public final static class Client {
		public final ConfigFloat standStatsTranslucency = new ConfigFloat(0.75F);
		public final ConfigBool standStatsInvertBnW = new ConfigBool(false);
		//public final ConfigEnum<ChooseLifeformScreen.ViewMode> viewModeGE = null;

		//public final ConfigEnum<PositionConfig> barsPosition = PositionConfig.TOP_LEFT;
		//public final ConfigEnum<PositionConfig> hotbarsPosition = PositionConfig.TOP_LEFT;
		//public final ConfigEnum<HudTextRender> hudTextRender = HudTextRender.FADE_OUT;
		//public final ConfigBool hudHotbarFold = true;
		//public final ConfigBool showLockedSlots = false;
		public final ConfigBool abilitySelectionWheel = new ConfigBool(true);

		//public final ConfigBool resolveShaders = true;
		//public final ConfigBool timeStopAnimation = true;
		public final ConfigBool standMotionTilt = new ConfigBool(true);
		//public final ConfigBool poseOnLmbRmb = true;
		//public final ConfigBool autoResolveActivation = true;
		//public final ConfigBool standOutline = true;
		public final ConfigBool standAimMarker = new ConfigBool(false);

		//public final ConfigBool menacingParticles = true;
		//public final ConfigBool characterVoiceLines = true;

		public final ConfigBool toggleDisableHotbars = new ConfigBool(false);

		public final ConfigBool thirdPersonHamonAura = new ConfigBool(true);
		//public final ConfigBool firstPersonHamonAura = new ConfigBool(true);
		//public final ConfigBool hamonAuraBlur = new ConfigBool(false);
	}

	public final static class ClientBroadcast {
		//public final ConfigEnum<HumanoidArm> standSide = HumanoidArm.LEFT;
		public final ConfigBool vampireGlowingEyes = new ConfigBool(true);
	}
	
	public static class Common {
		//public final ConfigBool keepStandOnDeath;
		//public final ConfigBool keepHamonOnDeath;
		//public final ConfigBool keepVampirismOnDeath;
		public final ConfigBool dropStandAsDisc = new ConfigBool(false);

		//public final ConfigBool hamonTempleSpawn;
		//public final ConfigBool meteoriteSpawn;
		//public final ConfigBool pillarManTempleSpawn;

		//public final ConfigFloat hamonDamageMultiplier;
		//public final ConfigBool hamonEnergyTicksDown;

		//public final ConfigFloat hamonPointsMultiplier;
		//public final ConfigFloat breathingTrainingMultiplier;
		//public final ConfigBool breathingTrainingDeterioration;
		//public final ConfigInt breathingHamonStatGap;
		//public final ConfigBool mixHamonTechniques;
		//public final ForgeConfigSpec.ConfigValue<List<? extends Integer>> techniqueSkillsRequirement;

		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> maxBloodMultiplier;
		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodDrainMultiplier;
		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodTickDown;
		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> bloodHealCost;
		//public final ConfigBool vampiresAggroMobs;
		//public final ConfigBool undeadMobsSunDamage;
		//public final ConfigInt vampirismCuringDuration;

		//public final ConfigInt arrowDurability;
		//public final ConfigInt arrowDurabilityBeetle;
		//public final ConfigInt standXpCostInitial;
		//public final ConfigInt standXpCostIncrease;
		//public final ConfigEnum<StandUtil.StandRandomPoolFilter> standRandomPoolFilter;
		//public final ForgeConfigSpec.ConfigValue<List<? extends String>> bannedStands;
		//private List<StandType<?>> bannedStandsSynced = null;
		//private List<ResourceLocation> bannedStandsResLocs;

		//public final ConfigBool abilitiesBreakBlocks;
		//public final ConfigFloat standDamageMultiplier;
		//public final ConfigFloat standResistanceMultiplier;
		//public final ConfigBool skipStandProgression;
		public final ConfigBool standStamina = new ConfigBool(true);
		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> resolveLvlPoints;
		//public final ConfigBool soulAscension;
		//public final ConfigInt timeStopChunkRange;
		//public final ConfigFloat timeStopDamageMultiplier;

		//public final ConfigBool endermenBeyondTimeSpace;
		//public final ConfigBool saveDestroyedBlocks;
		//public final ConfigBool spawnCocoJumboTurtle;
	}
	
	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ConfigGUIEventHandler {
		
		@SubscribeEvent
		public static void register(RegisterRotpConfigScreenTabEvent event) {
			event.register(RotpConfig.ID, (ConfigTabType configTab, ClientModSettingsScreen screen) -> {
				ModConfigInterface<RotpConfig.Client, RotpConfig.ClientBroadcast, RotpConfig.Common> config = JojoMod.config;
				ConfigGuiHelper helper = new ConfigGuiHelper(screen, RotpConfig.ID, config);
				
				RotpConfig.Client client = config.getClient();
				RotpConfig.ClientBroadcast clientBroadcast = config.getPlayerBroadcast(null);
				
				helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.hud"));
				helper.addBooleanOptionButton(client.abilitySelectionWheel, ModConfigType.CLIENT, "ability_selection_wheel");
				
				helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.stand"));
				helper.addBooleanOptionButton(config.getClient().standAimMarker, ModConfigType.CLIENT, "stand_aim_marker");
				helper.addBooleanOptionButton(config.getClient().standMotionTilt, ModConfigType.CLIENT, null);
				
				helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.hamon"));
				
				helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.vampirism"));
				
			});
		}
	}
	
}
