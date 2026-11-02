package com.github.standobyte.jojo.config;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.itemrender.ItemIconModels;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigTabType;
import com.github.standobyte.jojo.config.client.ConfigGuiHelper;
import com.github.standobyte.jojo.config.client.RegisterRotpConfigScreenTabEvent;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.core.ModConfig.CommonConfigState;
import com.github.standobyte.jojo.config.core.ModConfigType;
import com.github.standobyte.jojo.config.core.types.ClientConfigKeyBinding;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.config.core.types.ConfigEnum;
import com.github.standobyte.jojo.config.core.types.ConfigFloat;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.resolve.ClientResolveVisuals;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

		public final ConfigBool resolveShaders = new ConfigBool(true) {
			@Override
			public void set(Boolean value, CommonConfigState cfgState) {
				super.set(value, cfgState);
				ClientResolveVisuals.onColorShiftSettingUpdated(value);
			}
		};
		//public final ConfigBool timeStopAnimation = true;
		public final ConfigBool standMotionTilt = new ConfigBool(true);
		//public final ConfigBool poseOnLmbRmb = true;
		//public final ConfigBool autoResolveActivation = true;
		//public final ConfigBool standOutline = true;
		public final ConfigBool standAimMarker = new ConfigBool(false);
		public final ConfigBool standAura = new ConfigBool(false);

		//public final ConfigBool menacingParticles = true;
		//public final ConfigBool characterVoiceLines = true;

		public final ConfigBool toggleDisableHotbars = new ConfigBool(false);

		public final ConfigBool thirdPersonHamonAura = new ConfigBool(true);
		//public final ConfigBool firstPersonHamonAura = new ConfigBool(true);
		//public final ConfigBool hamonAuraBlur = new ConfigBool(false);


		public final ConfigBool toggleVisible_standBreaksBlocks = new ConfigBool(false);
		public final ClientConfigKeyBinding toggleKeybind_standBreaksBlocks = new ClientConfigKeyBinding(() -> {
			var inputHandler = InputHandler.getInstance();
			return inputHandler != null ? inputHandler.vanillaKeybinds.standToggle_breakBlocks : null;
		});

		public final ConfigBool toggleVisible_standPicksUpItems = new ConfigBool(false);
		public final ClientConfigKeyBinding toggleKeybind_standPicksUpItems = new ClientConfigKeyBinding(() -> {
			var inputHandler = InputHandler.getInstance();
			return inputHandler != null ? inputHandler.vanillaKeybinds.standToggle_pickUpItems : null;
		});

		public final ConfigBool toggleVisible_standMovesBeyondEffRange = new ConfigBool(false);
		public final ClientConfigKeyBinding toggleKeybind_standMovesBeyondEffRange = new ClientConfigKeyBinding(() -> {
			var inputHandler = InputHandler.getInstance();
			return inputHandler != null ? inputHandler.vanillaKeybinds.standToggle_moveBeyondEffRange : null;
		});
	}

	public final static class ClientBroadcast {
		public final ConfigEnum<HumanoidArm> standSide = new ConfigEnum<>(HumanoidArm.class, HumanoidArm.LEFT);
		public final ConfigEnum<StandIdleRotation> standIdleRotRelative = new ConfigEnum<>(StandIdleRotation.class, StandOffsetFromUser.CONFIG_DEFAULT);
		public final ConfigBool standBreaksBlocks = new ConfigBool(true);
		public final ConfigBool standPicksUpItems = new ConfigBool(true);
		public final ConfigBool standMovesBeyondEffRange = new ConfigBool(true);
		public final ConfigBool vampireGlowingEyes_tmp = new ConfigBool(false);
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
		//public final ConfigFloat standDamageMultiplier = new ConfigFloat(1).initNonNegative();
		public final ConfigFloat standExpGainMultiplier = new ConfigFloat(1).initNonNegative();
		//public final ConfigFloat standResistanceMultiplier;
		//public final ConfigBool skipStandProgression;
		public final ConfigEnum<BoolOrPlayerPref> standsBreakBlocks = new ConfigEnum<>(BoolOrPlayerPref.class, BoolOrPlayerPref.PLAYER_PREFERENCE);
		//public final ForgeConfigSpec.ConfigValue<List<? extends Double>> resolveLvlPoints;
		//public final ConfigBool soulAscension;
		//public final ConfigInt timeStopChunkRange;
		//public final ConfigFloat timeStopDamageMultiplier;
		public final ConfigBool standStamina = new ConfigBool(true);

		//public final ConfigBool endermenBeyondTimeSpace;
		//public final ConfigBool saveDestroyedBlocks;
		//public final ConfigBool spawnCocoJumboTurtle;
	}
	
	public enum StandIdleRotation { HEAD, BODY }
	
	public static boolean canStandBreakBlocks(LivingEntity playerUser) {
		var config = JojoMod.config;
		Common common = config.getCommon();
		return switch (common.standsBreakBlocks.get()) {
			case TRUE -> true;
			case FALSE -> false;
			case PLAYER_PREFERENCE -> {
				if (playerUser instanceof Player player) {
					ClientBroadcast broadcast = config.getPlayerBroadcast(player);
					yield broadcast.standBreaksBlocks.getAsBoolean();
				}
				yield true;
			}
		};
	}
	
	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class ConfigGUIEventHandler {
		
		@SubscribeEvent
		public static void register(RegisterRotpConfigScreenTabEvent event) {
			event.register(RotpConfig.ID, (ClientModSettingsScreen screen, ConfigTabType configTab, @Nullable StandType standInStandTab) -> {
				switch (configTab) {
					case CLIENT -> {
						ConfigGuiHelper helper = new ConfigGuiHelper(screen, RotpConfig.ID);
						helper.addModSettingsList(JojoMod.config);
						
						RotpConfig.Client client = JojoMod.config.getClient();
						RotpConfig.ClientBroadcast clientBroadcast = JojoMod.config.getPlayerBroadcast(null);
						
						helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.controls"));
						helper.addBooleanOptionButton(client.abilitySelectionWheel, ModConfigType.CLIENT, "ability_selection_wheel");
						
						helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.stand"));
						helper.addEnumOptionButton(clientBroadcast.standSide, ModConfigType.CLIENT_BROADCAST, null);
						helper.addEnumOptionButton(clientBroadcast.standIdleRotRelative, ModConfigType.CLIENT_BROADCAST, null);
						helper.addBooleanOptionButton(client.standAimMarker, ModConfigType.CLIENT, "stand_aim_marker");
						helper.addBooleanOptionButton(client.standMotionTilt, ModConfigType.CLIENT, null);
						helper.addBooleanOptionButton(client.standAura, ModConfigType.CLIENT, null);
						helper.addBooleanOptionButton(client.resolveShaders, ModConfigType.CLIENT, null);
						
						helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.hamon"));
						
						helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.vampirism"));
						helper.addBooleanOptionButton(clientBroadcast.vampireGlowingEyes_tmp, ModConfigType.CLIENT_BROADCAST, "vampire_glowing_eyes");
					}
					case COMMON -> {
						ConfigGuiHelper helper = new ConfigGuiHelper(screen, RotpConfig.ID);
						helper.addModSettingsList(JojoMod.config);
						
						RotpConfig.Common common = JojoMod.config.getCommon();
						
						helper.addCategoryTitle(Component.translatable("jojo_ripples.options.client.stand"));
						helper.addBooleanOrPlayerPreferenceOptionButton(common.standsBreakBlocks, ModConfigType.COMMON, "stands_break_blocks");
						helper.addBooleanOptionButton(common.dropStandAsDisc, ModConfigType.COMMON, "stand_disc");
						helper.addBooleanOptionButton(common.standStamina, ModConfigType.COMMON, "stand_stamina");
						//helper.addNumericField(common.standDamageMultiplier, ModConfigType.COMMON, null);
						helper.addNumericField(common.standExpGainMultiplier, ModConfigType.COMMON, null);
					}
					case STANDS -> {
						ConfigGuiHelper helper = new ConfigGuiHelper(screen, RotpConfig.ID);
						
						ModConfig<?, ?, ?> standConfig = ConfigGuiHelper.getStandConfig(standInStandTab);
						helper.generateStandConfigUI(RotpConfig.ID, standInStandTab, standConfig);
					}
				}
				
			});

			event.registerIcon(RotpConfig.ID, ItemIconModels.makeIconItem(ItemIconModels.MOD_LOGO));
		}
	}
	
}
