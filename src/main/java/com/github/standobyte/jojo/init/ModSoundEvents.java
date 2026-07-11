package com.github.standobyte.jojo.init;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// XXX sound subtitles
public class ModSoundEvents {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, JojoMod.MOD_ID);


	public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_HAT_THROW = SOUNDS.register("blade_hat_throw", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_HAT_SPINNING = register("blade_hat_spinning"); // this method does the same thing, it's just shorter
	public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_HAT_ENTITY_HIT = register("blade_hat_entity_hit");
	public static final DeferredHolder<SoundEvent, SoundEvent> AJA_STONE_CHARGING = register("aja_stone_charging");
	public static final DeferredHolder<SoundEvent, SoundEvent> AJA_STONE_BEAM = register("aja_stone_beam");
	public static final DeferredHolder<SoundEvent, SoundEvent> CLACKERS = register("clackers");
	public static final DeferredHolder<SoundEvent, SoundEvent> TOMMY_GUN_LOOP = register("tommy_gun_loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> TOMMY_GUN_NO_AMMO = register("tommy_gun_no_ammo");
	public static final DeferredHolder<SoundEvent, SoundEvent> MOLOTOV_THROW = register("molotov_throw");
	public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_THROW = register("knife_throw");
	public static final DeferredHolder<SoundEvent, SoundEvent> KNIVES_THROW = register("knives_throw");
	public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_HIT = register("knife_hit");
	public static final DeferredHolder<SoundEvent, SoundEvent> WATER_SPLASH = register("water_splash");
	public static final DeferredHolder<SoundEvent, SoundEvent> CLOTHES_SEWED = register("clothes_sewed");

	public static final DeferredHolder<SoundEvent, SoundEvent> WALKMAN_REWIND = register("walkman_rewind");

	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_WHITE = register("cassette_white");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_ORANGE = register("cassette_orange");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_MAGENTA = register("cassette_magenta");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_LIGHT_BLUE = register("cassette_light_blue");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_YELLOW = register("cassette_yellow");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_LIME = register("cassette_lime");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_PINK = register("cassette_pink");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_GRAY = register("cassette_gray");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_LIGHT_GRAY = register("cassette_light_gray");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_CYAN = register("cassette_cyan");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_PURPLE = register("cassette_purple");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_BLUE = register("cassette_blue");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_BROWN = register("cassette_brown");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_GREEN = register("cassette_green");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_RED = register("cassette_red");
	public static final DeferredHolder<SoundEvent, SoundEvent> CASSETTE_BLACK = register("cassette_black");

	public static final DeferredHolder<SoundEvent, SoundEvent> MAP_BOUGHT_METEORITE = register("map_bought_snowy");
	public static final DeferredHolder<SoundEvent, SoundEvent> MAP_BOUGHT_HAMON_TEMPLE = register("map_bought_mountain");
	public static final DeferredHolder<SoundEvent, SoundEvent> MAP_BOUGHT_PILLAR_MAN_TEMPLE = register("map_bought_jungle");

	public static final DeferredHolder<SoundEvent, SoundEvent> HEAVY_PUNCH = register("heavy_punch");

	public static final DeferredHolder<SoundEvent, SoundEvent> STONE_MASK_ACTIVATION_ENTITY = register("stone_mask_activation_entity");
	public static final DeferredHolder<SoundEvent, SoundEvent> STONE_MASK_ACTIVATION = register("stone_mask_activation");
	public static final DeferredHolder<SoundEvent, SoundEvent> STONE_MASK_DEACTIVATION = register("stone_mask_deactivation");

	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_BLOOD_DRAIN = register("vampire_blood_drain");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_SWIPE = register("vampire_swipe");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_CLAW_LACERATE = register("vampire_claw_lacerate");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_FREEZE = register("vampire_freeze");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_EVIL_ATMOSPHERE = register("vampire_dark_aura");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_CURE_START = register("vampire_cure_start");
	public static final DeferredHolder<SoundEvent, SoundEvent> VAMPIRE_CURE_END = register("vampire_cure_end");

	public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_DEVOUR = register("zombie_devour");
	public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_SWIPE = register("zombie_swipe");
	public static final DeferredHolder<SoundEvent, SoundEvent> ZOMBIE_CLAW_LACERATE = register("zombie_claw_lacerate");

	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_AWAKENING = register("pillar_man_awakening");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_HEAT_MODE = register("pillar_man_heat_mode");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_WIND_MODE = register("pillar_man_wind_mode");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_LIGHT_MODE = register("pillar_man_light_mode");

	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_ABSORPTION = register("pillar_man_absorption");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_SWING = register("pillar_man_swing");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_PUNCH = register("pillar_man_punch");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_STRONG_REGEN = register("pillar_man_strong_regen");
	public static final DeferredHolder<SoundEvent, SoundEvent> PILLAR_MAN_EVASION = register("pillar_man_evasion");

	public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_FILL_BOILING_BLOOD = register("bucket_fill_boiling_blood");
	public static final DeferredHolder<SoundEvent, SoundEvent> BUCKET_EMPTY_BOILING_BLOOD = register("bucket_empty_boiling_blood");
	public static final DeferredHolder<SoundEvent, SoundEvent> BOILING_BLOOD_POP = register("boiling_blood_pop");
	public static final DeferredHolder<SoundEvent, SoundEvent> BOILING_BLOOD_AMBIENT = register("boiling_blood_ambient");

	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SPARK = register("hamon_spark");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SPARKS_LONG = register("hamon_sparks_long");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SPARK_SHORT = register("hamon_spark_short");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_CONCENTRATION = register("hamon_concentration");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_HEALING = register("hamon_healing");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SYO_CHARGE = register("hamon_syo_charge");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SYO_PUNCH = register("hamon_syo_punch");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_SYO_SWING = register("hamon_syo_swing");
	public static final DeferredHolder<SoundEvent, SoundEvent> GLIDER_FLIGHT = register("glider_flight");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_DETECTOR = register("hamon_detector");
	public static final DeferredHolder<SoundEvent, SoundEvent> HAMON_REBUFF_PUNCH = register("hamon_rebuff_punch");

	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_SUMMON = register("stand_summon");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_UNSUMMON = register("stand_unsummon");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_DAMAGE_BLOCK = register("stand_damage_block");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_LIGHT = register("stand_punch_light");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_BARRAGE = register("stand_punch_barrage");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_HEAVY = register("stand_punch_heavy");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_HEAVY_CHARGED = register("stand_punch_heavy_charged");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_LEAP = register("stand_leap");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_SWING = register("stand_punch_swing");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_HEAVY_SWING = register("stand_punch_heavy_swing");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_BARRAGE_SWING = register("stand_punch_barrage_swing");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_CRY = register("stand_punch_cry");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_PUNCH_HEAVY_CRY = register("stand_punch_heavy_cry");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAND_BARRAGE_CRY = register("stand_barrage_cry");
	public static final DeferredHolder<SoundEvent, SoundEvent> VOICELINE_STAND_SUMMON = register("vl_stand_summon");

	public static final DeferredHolder<SoundEvent, SoundEvent> STAR_PLATINUM_STAR_FINGER = SOUNDS.register("star_platinum_star_finger", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> STAR_PLATINUM_ZOOM = SOUNDS.register("star_platinum_zoom", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> STAR_PLATINUM_ZOOM_CLICK = SOUNDS.register("star_platinum_zoom_click", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> STAR_PLATINUM_INHALE = SOUNDS.register("star_platinum_inhale", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> BEARING_SHOT = SOUNDS.register("bearing_shot", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> VOICELINE_STAR_FINGER = register("vl_star_finger");
	public static final DeferredHolder<SoundEvent, SoundEvent> VOICELINE_TIME_STOP = register("vl_time_stop");
	public static final DeferredHolder<SoundEvent, SoundEvent> VOICELINE_TIME_RESUME = register("vl_time_resume");

    public static final DeferredHolder<SoundEvent, SoundEvent> JOTARO_STAR_FINGER = SOUNDS.register("jotaro_star_finger", SoundEvent::createVariableRangeEvent);

	public static final DeferredHolder<SoundEvent, SoundEvent> TIME_STOP = SOUNDS.register("time_stop", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> TIME_RESUME = SOUNDS.register("time_resume", SoundEvent::createVariableRangeEvent);
	public static final DeferredHolder<SoundEvent, SoundEvent> TIME_STOP_BLINK = SOUNDS.register("time_stop_blink", SoundEvent::createVariableRangeEvent);

	public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_GREEN_TENTACLES = register("hierophant_green_tentacles");
	public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_GREEN_EMERALD_SPLASH = register("hierophant_green_emerald_splash");
	public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_GREEN_BARRIER_PLACED = register("hierophant_green_barrier_placed");
	public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_GREEN_BARRIER_RIPPED = register("hierophant_green_barrier_ripped");
	public static final DeferredHolder<SoundEvent, SoundEvent> HIEROPHANT_GREEN_GRAPPLE_CATCH = register("hierophant_green_grapple_catch");

	public static final DeferredHolder<SoundEvent, SoundEvent> ROAD_ROLLER_HIT = register("road_roller_hit");
	public static final DeferredHolder<SoundEvent, SoundEvent> ROAD_ROLLER_LAND = register("road_roller_land");

	public static final DeferredHolder<SoundEvent, SoundEvent> SILVER_CHARIOT_DASH = register("silver_chariot_dash");
	public static final DeferredHolder<SoundEvent, SoundEvent> SILVER_CHARIOT_RAPIER_SHOT = register("silver_chariot_rapier_shot");
	public static final DeferredHolder<SoundEvent, SoundEvent> SILVER_CHARIOT_ARMOR_OFF = register("silver_chariot_armor_off");

	public static final DeferredHolder<SoundEvent, SoundEvent> MAGICIANS_RED_FIRE_BLAST = register("magicians_red_fire_ability");
	public static final Supplier<SoundEvent> MAGICIANS_RED_FIREBALL = () -> SoundEvents.FIRECHARGE_USE;
	public static final DeferredHolder<SoundEvent, SoundEvent> MAGICIANS_RED_CROSSFIRE_HURRICANE = MAGICIANS_RED_FIRE_BLAST;
	public static final DeferredHolder<SoundEvent, SoundEvent> MAGICIANS_RED_RED_BIND = MAGICIANS_RED_FIRE_BLAST;

	public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_DIAMOND_FIX_STARTED = register("crazy_diamond_fix_started");
	public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_DIAMOND_FIX_LOOP = register("crazy_diamond_fix_loop");
	public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_DIAMOND_FIX_ENDED = register("crazy_diamond_fix_ended");
	public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_DIAMOND_BULLET_SHOT = register("crazy_diamond_bullet_shot");
	public static final DeferredHolder<SoundEvent, SoundEvent> CRAZY_DIAMOND_BLOOD_CUTTER_SHOT = register("crazy_diamond_blood_cutter_shot");
	public static final DeferredHolder<SoundEvent, SoundEvent> ANGELO_ROCK_GRUNT = register("angelo_rock_grunt");

	public static final DeferredHolder<SoundEvent, SoundEvent> GOLD_EXPERIENCE_LIFE_START = register("gold_experience_life_start");
	public static final DeferredHolder<SoundEvent, SoundEvent> GOLD_EXPERIENCE_LIFE_REVERT = register("gold_experience_life_revert");
	public static final DeferredHolder<SoundEvent, SoundEvent> GOLD_EXPERIENCE_LIFE_ITEM = register("gold_experience_life_item");
	public static final DeferredHolder<SoundEvent, SoundEvent> GOLD_EXPERIENCE_HEAL = register("gold_experience_heal");
	
	
	private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
		return SOUNDS.register(name, SoundEvent::createVariableRangeEvent);
	}
	
}
