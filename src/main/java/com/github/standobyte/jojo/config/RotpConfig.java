package com.github.standobyte.jojo.config;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen;
import com.github.standobyte.jojo.config.client.ClientModSettingsScreen.ConfigType;
import com.github.standobyte.jojo.config.client.RegisterRotpConfigScreenTabEvent;
import com.github.standobyte.jojo.config.client.ScrollingStringButton;
import com.github.standobyte.jojo.config.core.ConfigOption;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.config.core.types.ConfigEnum;
import com.github.standobyte.jojo.config.core.types.ConfigFloat;
import com.github.standobyte.jojo.config.internal.cfgtypes.ClientFileConfig;
import com.github.standobyte.jojo.core.JojoMod;

import it.unimi.dsi.fastutil.objects.Object2CharArrayMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
			event.register(RotpConfig.ID, (ConfigType configTab, ClientModSettingsScreen screen) -> {
				int i = 0;
				int yOffset = 0;
				
				ModConfig<Client, ClientBroadcast, Common> settings = (ModConfig<Client, ClientBroadcast, Common>) JojoMod.config;
				
				// HUD settings
				
				yOffset += 10;
				if (i % 2 == 1) ++i;
				screen.addCategoryTitle(
						Component.translatable("jojo_ripples.options.client.hud"), 
						calcButtonY(screen, i) + yOffset);
				yOffset += 15;

				Setting<Boolean> abilitySelectionWheel = new BooleanSetting(settings.clientConfig, settings.getClient().abilitySelectionWheel, settings.modId)
						.withIcon(ClientModSettingsScreen.toIconPath("ability_selection_wheel"));
				screen.addConfigWidget(abilitySelectionWheel.createButton(
						calcButtonX(screen, i), calcButtonY(screen, i++) + yOffset, 
						150, 20, screen, i));

				
				
				// Stand settings

				yOffset += 10;
				if (i % 2 == 1) ++i;
				screen.addCategoryTitle(
						Component.translatable("jojo_ripples.options.client.stand"), 
						calcButtonY(screen, i) + yOffset);
				yOffset += 15;

				Setting<Boolean> standAimMarker = new BooleanSetting(settings.clientConfig, settings.getClient().standAimMarker, settings.modId)
						.withIcon(ClientModSettingsScreen.toIconPath("stand_aim_marker"), iconPath -> new IconGlyphInfo(new GuiIcon(iconPath, 17, 17), 17, 17, 0, -5, 5));
				screen.addConfigWidget(standAimMarker.createButton(
						calcButtonX(screen, i), calcButtonY(screen, i++) + yOffset, 
						150, 20, screen, i));
				
				Setting<Boolean> standMotionTilt = new BooleanSetting(settings.clientConfig, settings.getClient().standMotionTilt, settings.modId);
				screen.addConfigWidget(standMotionTilt.createButton(
						calcButtonX(screen, i), calcButtonY(screen, i++) + yOffset, 
						150, 20, screen, i));
				
				
				
				// Hamon settings

				yOffset += 10;
				if (i % 2 == 1) ++i;
				screen.addCategoryTitle(
						Component.translatable("jojo_ripples.options.client.hamon"), 
						calcButtonY(screen, i) + yOffset);
				yOffset += 15;
				
				
				
				// Vampirism settings

				yOffset += 10;
				if (i % 2 == 1) ++i;
				screen.addCategoryTitle(
						Component.translatable("jojo_ripples.options.client.vampirism"), 
						calcButtonY(screen, i) + yOffset);
				yOffset += 15;
			});
		}

		@Deprecated
		protected static int calcButtonX(Screen screen, int i) {
			return screen.width / 2 - 155 + i % 2 * 160;
		}

		@Deprecated
		protected static int calcButtonY(Screen screen, int i) {
			return screen.height / 6 - 12 + 24 * (i >> 1);
		}
		



		@Deprecated
		protected static Object2CharMap<ResourceLocation> iconSymbols = new Object2CharArrayMap<>();

		@Deprecated
		public static abstract class Setting<T> {
			protected ClientFileConfig<?, ?> settings;
			protected ConfigOption<T> option;
			protected Component name;
			protected Component nameWithSprite;
			protected Component tooltip;
			@Nullable protected ResourceLocation sprite;
			protected char spriteCode;
			
			protected boolean broadcast = false;
			
			public Setting(ClientFileConfig<?, ?> settings, ConfigOption<T> configOption, String modId) {
				this.settings = settings;
				this.option = configOption;
				this.name = Component.translatable(modId + ".config.client." + configOption.getFieldName());
				this.tooltip = Component.translatable(modId + ".config.client." + configOption.getFieldName() + ".tooltip");
				this.nameWithSprite = name;
			}
			
			public Setting<T> withIcon(ResourceLocation sprite) {
				return withIcon(sprite, iconPath -> new IconGlyphInfo(new GuiIcon(iconPath, 16, 16), 16, 16, 0, -4, 4));
			}
			
			public Setting<T> withIcon(ResourceLocation sprite, Function<ResourceLocation, IconGlyphInfo> createGlyph) {
				this.sprite = sprite;
				if (sprite != null) {
					spriteCode = iconSymbols.computeIfAbsent(sprite, (ResourceLocation iconPath) -> 
							IconGlyphsCache.makeCharCodeFor(createGlyph.apply(iconPath)));
					this.nameWithSprite = Component.literal(String.valueOf(spriteCode)).append(name);
				}
				return this;
			}

			public Setting<T> setBroadcasted() {
				this.broadcast = true;
				return this;
			}
			
			protected Component nameWithSprite() {
				return sprite != null && IconSymbols.spriteExists(spriteCode) ? nameWithSprite : name;
			}
			
			public T get() {
				return option.get();
			}
			
			public void set(T value) {
				option.set(value);
			}

			public abstract Button createButton(int x, int y, int width, int height, Screen screen, int buttonI);
		}

		@Deprecated
		public static class BooleanSetting extends Setting<Boolean> {

			public BooleanSetting(ClientFileConfig<?, ?> settings, ConfigBool configOption, String modId) {
				super(settings, configOption, modId);
			}

			@Override
			public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
				return new ScrollingStringButton(
						x, y, width, height,
						CommonComponents.optionStatus(nameWithSprite(), get()), 
						button -> {
							option.set(!option.get());
							button.setMessage(CommonComponents.optionStatus(nameWithSprite(), get()));
							
							settings.saveToFileSystem();
							if (broadcast) {
								settings.sendToServer();
							}
						},
						Tooltip.create(tooltip))
						.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
			}
		}

		@Deprecated
		public static class EnumSetting<T extends Enum<T>> extends Setting<T> {
			protected Class<T> enumClass;
			protected String prefix = "jojo_ripples.config.client.option.";

			public EnumSetting(ClientFileConfig<?, ?> settings, ConfigEnum<T> configOption, String modId) {
				super(settings, configOption, modId);
				this.enumClass = configOption.enumClass;
			}

			public EnumSetting<T> prefix(String prefix) {
				this.prefix += prefix;
				return this;
			}

			@Override
			public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
				return new ScrollingStringButton(
						x, y, width, height,
						Component.translatable("options.generic_value", nameWithSprite(), getValueMessage(get())), 
						button -> {
							T[] values = enumClass.getEnumConstants();
							T val = get();
							T nextVal = values[(val.ordinal() + 1) % values.length];
							option.set(nextVal);
							button.setMessage(Component.translatable("options.generic_value", nameWithSprite(), getValueMessage(nextVal)));

							settings.saveToFileSystem();
							if (broadcast) {
								settings.sendToServer();
							}
						},
						Tooltip.create(tooltip))
						.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
			}

			private Component getValueMessage(T value) {
				return Component.translatable(prefix + value.name().toLowerCase());
			}
	    }
		
	}
	
}
