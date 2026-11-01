package com.github.standobyte.jojo.init.power;

import static com.github.standobyte.jojo.init.power.ModStandsMinor.hideStand;

import com.github.standobyte.jojo.config.core.RegisterRotpConfigEvent;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.init.SimpleTagKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojoimpl.stands.StandInitAerosmith;
import com.github.standobyte.jojoimpl.stands.StandInitBadCompany;
import com.github.standobyte.jojoimpl.stands.StandInitCMoon;
import com.github.standobyte.jojoimpl.stands.StandInitCrazyDiamond;
import com.github.standobyte.jojoimpl.stands.StandInitDiverDown;
import com.github.standobyte.jojoimpl.stands.StandInitEchoes;
import com.github.standobyte.jojoimpl.stands.StandInitFooFighters;
import com.github.standobyte.jojoimpl.stands.StandInitGoldExperience;
import com.github.standobyte.jojoimpl.stands.StandInitHarvest;
import com.github.standobyte.jojoimpl.stands.StandInitHeavensDoor;
import com.github.standobyte.jojoimpl.stands.StandInitHermitPurple;
import com.github.standobyte.jojoimpl.stands.StandInitHierophantGreen;
import com.github.standobyte.jojoimpl.stands.StandInitKillerQueen;
import com.github.standobyte.jojoimpl.stands.StandInitKingCrimson;
import com.github.standobyte.jojoimpl.stands.StandInitKiss;
import com.github.standobyte.jojoimpl.stands.StandInitMadeInHeaven;
import com.github.standobyte.jojoimpl.stands.StandInitMagiciansRed;
import com.github.standobyte.jojoimpl.stands.StandInitPurpleHaze;
import com.github.standobyte.jojoimpl.stands.StandInitSexPistols;
import com.github.standobyte.jojoimpl.stands.StandInitSilverChariot;
import com.github.standobyte.jojoimpl.stands.StandInitSoftAndWet;
import com.github.standobyte.jojoimpl.stands.StandInitSpiceGirl;
import com.github.standobyte.jojoimpl.stands.StandInitStarPlatinum;
import com.github.standobyte.jojoimpl.stands.StandInitStickyFingers;
import com.github.standobyte.jojoimpl.stands.StandInitStoneFree;
import com.github.standobyte.jojoimpl.stands.StandInitTheHand;
import com.github.standobyte.jojoimpl.stands.StandInitTheWorld;
import com.github.standobyte.jojoimpl.stands.StandInitWeatherReport;
import com.github.standobyte.jojoimpl.stands.StandInitWhitesnake;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// XXX (data-driven stands) test the stand datapack configs
// XXX add a way to ban hardcoded stands
public class ModStands {
	public static final DeferredRegister<StandType> DEFAULT_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, JojoMod.MOD_ID);
	
	public static InputUseVanillaMapping USE_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.use_special_ability");
	public static InputUseVanillaMapping SWITCH_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.ability_hotbar");
	
	
	public static final SimpleTagKey<StandType> PLAYER_CAN_GET_FROM_ARROW = SimpleTagKey.create(StandType.class, JojoMod.resLoc("arrow"));
	public static final SimpleTagKey<StandType> DONT_ADD_DISC_TO_MAIN_CREATIVE_TAB = SimpleTagKey.create(StandType.class, JojoMod.resLoc("creative_disc"));
	@Deprecated
	public static final SimpleTagKey<StandType> EXCLUDE_FROM_CREATIVE_TAB = DONT_ADD_DISC_TO_MAIN_CREATIVE_TAB;
	public static final SimpleTagKey<StandType> NO_AUTOCOMPLETE_IN_CONSOLE_COMMANDS = SimpleTagKey.create(StandType.class, JojoMod.resLoc("commands"));

	public static final SimpleTagKey<StandType> GIVE_ON_FAILED_VIRUS_TRIAL = SimpleTagKey.create(StandType.class, JojoMod.resLoc("virus_fail"));
	public static final SimpleTagKey<StandType> METEORITE_INHABITANT_MOBS = SimpleTagKey.create(StandType.class, JojoMod.resLoc("meteorite"));
	
	
	// Adding all the abilities and skills takes quite a few lines, so I decided to put each into a separate file.
	// Makes it a bit easier to compare them between each other too.
	
	public static final DeferredHolder<StandType, EntityStandType> STAR_PLATINUM = DEFAULT_STANDS.register("star_platinum", StandInitStarPlatinum::create);
	public static final DeferredHolder<StandType, EntityStandType> HIEROPHANT_GREEN = DEFAULT_STANDS.register("hierophant_green", StandInitHierophantGreen::create);
	public static final DeferredHolder<StandType, StandType> HERMIT_PURPLE = hideStand(DEFAULT_STANDS.register("hermit_purple", StandInitHermitPurple::create));
	public static final DeferredHolder<StandType, EntityStandType> MAGICIANS_RED = hideStand(DEFAULT_STANDS.register("magicians_red", StandInitMagiciansRed::create));
	public static final DeferredHolder<StandType, EntityStandType> SILVER_CHARIOT = hideStand(DEFAULT_STANDS.register("silver_chariot", StandInitSilverChariot::create));
	public static final DeferredHolder<StandType, EntityStandType> THE_WORLD = DEFAULT_STANDS.register("the_world", StandInitTheWorld::create);
	
	public static final DeferredHolder<StandType, EntityStandType> CRAZY_DIAMOND = DEFAULT_STANDS.register("crazy_diamond", StandInitCrazyDiamond::create);
	public static final DeferredHolder<StandType, EntityStandType> THE_HAND = hideStand(DEFAULT_STANDS.register("the_hand", StandInitTheHand::create));
	public static final DeferredHolder<StandType, EntityStandType> ECHOES = hideStand(DEFAULT_STANDS.register("echoes", StandInitEchoes::create));
	public static final DeferredHolder<StandType, EntityStandType> HEAVENS_DOOR = hideStand(DEFAULT_STANDS.register("heavens_door", StandInitHeavensDoor::create));
	public static final DeferredHolder<StandType, EntityStandType> KILLER_QUEEN = hideStand(DEFAULT_STANDS.register("killer_queen", StandInitKillerQueen::create));
	public static final DeferredHolder<StandType, StandType> BAD_COMPANY = hideStand(DEFAULT_STANDS.register("bad_company", StandInitBadCompany::create));
	public static final DeferredHolder<StandType, StandType> HARVEST = hideStand(DEFAULT_STANDS.register("harvest", StandInitHarvest::create));
	
	public static final DeferredHolder<StandType, EntityStandType> GOLD_EXPERIENCE = hideStand(DEFAULT_STANDS.register("gold_experience", StandInitGoldExperience::create));
	public static final DeferredHolder<StandType, EntityStandType> STICKY_FINGERS = hideStand(DEFAULT_STANDS.register("sticky_fingers", StandInitStickyFingers::create));
	public static final DeferredHolder<StandType, StandType> SEX_PISTOLS = hideStand(DEFAULT_STANDS.register("sex_pistols", StandInitSexPistols::create));
	public static final DeferredHolder<StandType, EntityStandType> AEROSMITH = hideStand(DEFAULT_STANDS.register("aerosmith", StandInitAerosmith::create));
	public static final DeferredHolder<StandType, EntityStandType> PURPLE_HAZE = hideStand(DEFAULT_STANDS.register("purple_haze", StandInitPurpleHaze::create));
	public static final DeferredHolder<StandType, EntityStandType> SPICE_GIRL = hideStand(DEFAULT_STANDS.register("spice_girl", StandInitSpiceGirl::create));
	public static final DeferredHolder<StandType, EntityStandType> KING_CRIMSON = hideStand(DEFAULT_STANDS.register("king_crimson", StandInitKingCrimson::create));
	
	public static final DeferredHolder<StandType, EntityStandType> STONE_FREE = hideStand(DEFAULT_STANDS.register("stone_free", StandInitStoneFree::create));
	public static final DeferredHolder<StandType, EntityStandType> KISS = hideStand(DEFAULT_STANDS.register("kiss", StandInitKiss::create));
	public static final DeferredHolder<StandType, EntityStandType> WEATHER_REPORT = hideStand(DEFAULT_STANDS.register("weather_report", StandInitWeatherReport::create));
	public static final DeferredHolder<StandType, EntityStandType> DIVER_DOWN = hideStand(DEFAULT_STANDS.register("diver_down", StandInitDiverDown::create));
	public static final DeferredHolder<StandType, StandType> FOO_FIGHTERS = hideStand(DEFAULT_STANDS.register("foo_fighters", StandInitFooFighters::create));
	public static final DeferredHolder<StandType, EntityStandType> WHITESNAKE = hideStand(DEFAULT_STANDS.register("whitesnake", StandInitWhitesnake::create));
	public static final DeferredHolder<StandType, EntityStandType> C_MOON = hideStand(DEFAULT_STANDS.register("c_moon", StandInitCMoon::create));
	public static final DeferredHolder<StandType, EntityStandType> MADE_IN_HEAVEN = hideStand(DEFAULT_STANDS.register("made_in_heaven", StandInitMadeInHeaven::create));
	
	public static final DeferredHolder<StandType, EntityStandType> SOFT_AND_WET = hideStand(DEFAULT_STANDS.register("soft_and_wet", StandInitSoftAndWet::create));
	
	static {
		PLAYER_CAN_GET_FROM_ARROW.add(STAR_PLATINUM);
		PLAYER_CAN_GET_FROM_ARROW.add(CRAZY_DIAMOND);
		//PLAYER_CAN_GET_FROM_ARROW.add(THE_WORLD);
		//PLAYER_CAN_GET_FROM_ARROW.add(HIEROPHANT_GREEN);
	}
	
	
	public static void addStandConfigs(RegisterRotpConfigEvent event) {
		event.generateStandConfig(ModStands.CRAZY_DIAMOND);
		event.generateStandConfig(ModStands.STAR_PLATINUM);
		event.generateStandConfig(ModStands.THE_WORLD);
		event.generateStandConfig(ModStands.HIEROPHANT_GREEN);
	}
}
