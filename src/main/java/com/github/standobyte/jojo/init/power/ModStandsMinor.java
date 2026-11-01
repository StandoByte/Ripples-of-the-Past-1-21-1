package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStandsMinor {
	public static final DeferredRegister<StandType> ENCOUNTER_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, "encounter");
	public static final DeferredRegister<StandType> METEORITE_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, "meteorite");
	
	public static final DeferredHolder<StandType, StandType> STRAY_CAT = hideStand(ENCOUNTER_STANDS.register("stray_cat", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> MR_PRESIDENT = hideStand(ENCOUNTER_STANDS.register("mr_president", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> TOWER_OF_GRAY = hideStand(ENCOUNTER_STANDS.register("tower_of_gray", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> DARK_BLUE_MOON = hideStand(ENCOUNTER_STANDS.register("dark_blue_moon", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> EBONY_DEVIL = hideStand(ENCOUNTER_STANDS.register("ebony_devil", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> YELLOW_TEMPERANCE = hideStand(ENCOUNTER_STANDS.register("yellow_temperance", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> EMPEROR = hideStand(ENCOUNTER_STANDS.register("emperor", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> SUN = hideStand(ENCOUNTER_STANDS.register("sun", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> DEATH_13 = hideStand(ENCOUNTER_STANDS.register("death_13", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> HIGH_PRIESTESS = hideStand(ENCOUNTER_STANDS.register("high_priestess", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> GEB = hideStand(ENCOUNTER_STANDS.register("geb", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> BASTET = hideStand(ENCOUNTER_STANDS.register("bastet", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> ANUBIS = hideStand(ENCOUNTER_STANDS.register("anubis", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> CREAM = hideStand(ENCOUNTER_STANDS.register("cream", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> AQUA_NECKLACE = hideStand(ENCOUNTER_STANDS.register("aqua_necklace", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> SURFACE = hideStand(ENCOUNTER_STANDS.register("surface", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> PEARL_JAM = hideStand(ENCOUNTER_STANDS.register("pearl_jam", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> RATT = hideStand(ENCOUNTER_STANDS.register("ratt", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> BOY_II_MAN = hideStand(ENCOUNTER_STANDS.register("boy_ii_man", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> HIGHWAY_STAR = hideStand(ENCOUNTER_STANDS.register("highway_star", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> BLACK_SABBATH = hideStand(ENCOUNTER_STANDS.register("black_sabbath", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> KRAFT_WORK = hideStand(ENCOUNTER_STANDS.register("kraft_work", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> LITTLE_FEET = hideStand(ENCOUNTER_STANDS.register("little_feet", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> MAN_IN_THE_MIRROR = hideStand(ENCOUNTER_STANDS.register("man_in_the_mirror", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> THE_GRATEFUL_DEAD = hideStand(ENCOUNTER_STANDS.register("the_grateful_dead", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> BEACH_BOY = hideStand(ENCOUNTER_STANDS.register("beach_boy", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> WHITE_ALBUM = hideStand(ENCOUNTER_STANDS.register("white_album", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> NOTORIOUS_BIG = hideStand(ENCOUNTER_STANDS.register("notorious_big", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> METALLICA = hideStand(ENCOUNTER_STANDS.register("metallica", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> CLASH = hideStand(ENCOUNTER_STANDS.register("clash", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> GREEN_DAY = hideStand(ENCOUNTER_STANDS.register("green_day", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> OASIS = hideStand(ENCOUNTER_STANDS.register("oasis", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> JUMPIN_JACK_FLASH = hideStand(ENCOUNTER_STANDS.register("jumpin_jack_flash", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> LIMP_BIZKIT = hideStand(ENCOUNTER_STANDS.register("limp_bizkit", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> SURVIVOR = hideStand(ENCOUNTER_STANDS.register("survivor", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> JAIL_HOUSE_LOCK = hideStand(ENCOUNTER_STANDS.register("jail_house_lock", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> FUN_FUN_FUN = hideStand(ENCOUNTER_STANDS.register("fun_fun_fun", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, EntityStandType> BORN_THIS_WAY = hideStand(ENCOUNTER_STANDS.register("born_this_way", id -> new EntityStandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	public static final DeferredHolder<StandType, StandType> PAPER_MOON_KING = hideStand(ENCOUNTER_STANDS.register("paper_moon_king", id -> new StandType(
			new StandStats.Builder()
			.power(10)
			.speed(10)
			.range(10, 10)
			.durability(10)
			.precision(10)
			.build(),

			new MovesetBuilder()

			, id)
			.discTooltipExperimental().init(stand -> stand.discCategoryPriority += 1000)));
	
	
	public static <H extends Holder<StandType>> H hideStand(H standType) {
		ModStands.DONT_ADD_DISC_TO_MAIN_CREATIVE_TAB.add(standType);
		ModStands.NO_AUTOCOMPLETE_IN_CONSOLE_COMMANDS.add(standType);
		return standType;
	}
	
	public static <H extends Holder<StandType>> H meteoriteStand(H standType) {
		ModStands.DONT_ADD_DISC_TO_MAIN_CREATIVE_TAB.add(standType);
		ModStands.NO_AUTOCOMPLETE_IN_CONSOLE_COMMANDS.add(standType);
		ModStands.GIVE_ON_FAILED_VIRUS_TRIAL.add(standType);
		ModStands.METEORITE_INHABITANT_MOBS.add(standType);
		return standType;
	}
	
}
