package com.github.standobyte.jojo.init.power;

import static com.github.standobyte.jojo.core.JojoRegistries.ABILITY_TYPES;

import java.util.function.Supplier;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;
import com.github.standobyte.jojoimpl.stands._entitybase.StandBearingShotAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityBarrageAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityGrabAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityGrabReleaseAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityGrabThrowAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchChargedAbility;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityManualControlToggle;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityPunchAbility;
import com.github.standobyte.jojoimpl.stands._helditems.abilities.SwapStandHandItemsAbility;
import com.github.standobyte.jojoimpl.stands._helditems.abilities.SwapUserStandItemsAbility;
import com.github.standobyte.jojoimpl.stands._helditems.abilities.TossStandItemAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDAnchorBlockAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDAngeloRockPunchInput;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBloodCutterAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDHealAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDLeaveObjectPunchInput;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDMisshapingPunchEffect;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDMisshapingPunchInput;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRepairItemAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRevertEntityAndBlocksAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDTerrainWallAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDUncraftItemAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.DriedBloodDropsEffect;
import com.github.standobyte.jojoimpl.stands.hierophant.HierophantPuppetAbility;
import com.github.standobyte.jojoimpl.stands.hierophant.HierophantPuppetEffect;
import com.github.standobyte.jojoimpl.stands.starplatinum.HeavyPunchUppercutAbility;
import com.github.standobyte.jojoimpl.stands.starplatinum.StarFingerAbility;
import com.github.standobyte.jojoimpl.stands.starplatinum.StarFingerSwipeAbility;
import com.github.standobyte.jojoimpl.stands.starplatinum.StarInhaleAbility;
import com.github.standobyte.jojoimpl.stands.theworld.TimeStopAbility;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStandAbilities {
	public static final DeferredRegister<StandEffectType<?>> STAND_EFFECT_TYPES = DeferredRegister.create(JojoRegistries.STAND_EFFECTS_REG, JojoMod.MOD_ID);
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityManualControlToggle>> MANUAL_CONTROL = ABILITY_TYPES.register(
			"stand_manual_control", key -> new AbilityType<>(key, StandEntityManualControlToggle::new));
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<SwapUserStandItemsAbility>> ITEMS_SWAP_W_USER = ABILITY_TYPES.register(
			"stand_items_swap_w_user", key -> new AbilityType<>(key, SwapUserStandItemsAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<SwapStandHandItemsAbility>> ITEMS_SWAP_HANDS = ABILITY_TYPES.register(
			"stand_items_swap_hands", key -> new AbilityType<>(key, SwapStandHandItemsAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<TossStandItemAbility>> ITEM_TOSS = ABILITY_TYPES.register(
			"stand_item_toss", key -> new AbilityType<>(key, TossStandItemAbility::new));
	
//	public static final DeferredHolder<AbilityType<?>, AbilityType<StandAttackWithItemAbility>> ITEM_ATTACK = ABILITY_TYPES.register(
//			"stand_item_attack", key -> new AbilityType<>(key, StandAttackWithItemAbility::new));
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityPunchAbility>> PUNCH = ABILITY_TYPES.register(
			"stand_punch", key -> new AbilityType<>(key, StandEntityPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityBarrageAbility>> BARRAGE = ABILITY_TYPES.register(
			"stand_barrage", key -> new AbilityType<>(key, StandEntityBarrageAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityHeavyPunchAbility>> HEAVY_PUNCH = ABILITY_TYPES.register(
			"stand_heavy_punch", key -> new AbilityType<>(key, StandEntityHeavyPunchAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityHeavyPunchChargedAbility>> HEAVY_CHARGED = ABILITY_TYPES.register(
			"stand_heavy_charged", key -> new AbilityType<>(key, StandEntityHeavyPunchChargedAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityGrabAbility>> GRAB = ABILITY_TYPES.register(
			"stand_grab", key -> new AbilityType<>(key, StandEntityGrabAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> GRAB_RELEASE = ABILITY_TYPES.register(
			"stand_grab_release", key -> new AbilityType<>(key, StandEntityGrabReleaseAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandEntityGrabThrowAbility>> GRAB_THROW = ABILITY_TYPES.register(
			"stand_grab_throw", key -> new AbilityType<>(key, StandEntityGrabThrowAbility::new));

	
	public static final DeferredHolder<AbilityType<?>, AbilityType<HeavyPunchUppercutAbility>> HEAVY_UPPERCUT = ABILITY_TYPES.register(
			"stand_heavy_uppercut", key -> new AbilityType<>(key, HeavyPunchUppercutAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StarFingerAbility>> SP_STAR_FINGER = ABILITY_TYPES.register(
			"star_finger", key -> new AbilityType<>(key, StarFingerAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StarFingerSwipeAbility>> SP_STAR_FINGER_SWIPE = ABILITY_TYPES.register(
			"star_finger_swipe", key -> new AbilityType<>(key, StarFingerSwipeAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StarInhaleAbility>> SP_INHALE = ABILITY_TYPES.register(
			"inhale", key -> new AbilityType<>(key, StarInhaleAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<StandBearingShotAbility>> BEARING_SHOT = ABILITY_TYPES.register(
			"bearing_shot", key -> new AbilityType<>(key, StandBearingShotAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<TimeStopAbility>> TIME_STOP = ABILITY_TYPES.register(
			"time_stop", key -> new AbilityType<>(key, TimeStopAbility::new));
	
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<HierophantPuppetAbility>> HG_PUPPET = ABILITY_TYPES.register(
			"puppet", key -> new AbilityType<>(key, HierophantPuppetAbility::new));

	public static final DeferredHolder<StandEffectType<?>, StandEffectType<HierophantPuppetEffect>> EFFECT_HG_PUPPET = STAND_EFFECT_TYPES.register(
			"hg_puppet", key -> new StandEffectType<>(key, HierophantPuppetEffect::new));

	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDBloodCutterAbility>> CD_BLOOD_CUTTER = ABILITY_TYPES.register(
			"blood_cutter", key -> new AbilityType<>(key, CrazyDBloodCutterAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDBlockBulletAbility>> CD_BLOCK_BULLET = ABILITY_TYPES.register(
			"block_bullet", key -> new AbilityType<>(key, CrazyDBlockBulletAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDRevertEntityAndBlocksAbility>> CD_REVERT_STATE = ABILITY_TYPES.register(
			"revert_state", key -> new AbilityType<>(key, CrazyDRevertEntityAndBlocksAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDHealAbility>> CD_HEAL = ABILITY_TYPES.register(
			"heal", key -> new AbilityType<>(key, CrazyDHealAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDRestoreTerrainAbility>> CD_RESTORE_TERRAIN = ABILITY_TYPES.register(
			"restore_terrain", key -> new AbilityType<>(key, CrazyDRestoreTerrainAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDAnchorBlockAbility>> CD_ANCHOR_MOVE = ABILITY_TYPES.register(
			"anchor_move", key -> new AbilityType<>(key, CrazyDAnchorBlockAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDTerrainWallAbility>> CD_WALL_FROM_TERRAIN = ABILITY_TYPES.register(
			"wall_from_terrain", key -> new AbilityType<>(key, CrazyDTerrainWallAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDRepairItemAbility>> CD_REPAIR_ITEM = ABILITY_TYPES.register(
			"repair_item", key -> new AbilityType<>(key, CrazyDRepairItemAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDUncraftItemAbility>> CD_UNCRAFT_ITEM = ABILITY_TYPES.register(
			"uncraft_item", key -> new AbilityType<>(key, CrazyDUncraftItemAbility::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDLeaveObjectPunchInput>> CD_LEAVE_OBJECT_ON_PUNCH = ABILITY_TYPES.register(
			"leave_object", key -> new AbilityType<>(key, CrazyDLeaveObjectPunchInput::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDMisshapingPunchInput>> CD_DISFIGURE_ON_PUNCH = ABILITY_TYPES.register(
			"misshape", key -> new AbilityType<>(key, CrazyDMisshapingPunchInput::new));
	
	public static final DeferredHolder<AbilityType<?>, AbilityType<CrazyDAngeloRockPunchInput>> CD_ANGELO_ROCK_ON_PUNCH = ABILITY_TYPES.register(
			"angelo_rock", key -> new AbilityType<>(key, CrazyDAngeloRockPunchInput::new));

	public static final DeferredHolder<StandEffectType<?>, StandEffectType<DriedBloodDropsEffect>> EFFECT_CD_BLOOD_DROPS = STAND_EFFECT_TYPES.register(
			"cd_blood_drops", key -> new StandEffectType<>(key, DriedBloodDropsEffect::new));

//	public static final DeferredHolder<StandEffectType<?>, StandEffectType<CrazyDLeaveObjectPunchEffect>> EFFECT_CD_PUNCH_LEAVE_OBJECT = STAND_EFFECT_TYPES.register(
//			"cd_punch_leave_object", key -> new StandEffectType<>(key, CrazyDLeaveObjectPunchEffect::new));
//
	public static final DeferredHolder<StandEffectType<?>, StandEffectType<CrazyDMisshapingPunchEffect>> EFFECT_CD_PUNCH_MISSHAPING = STAND_EFFECT_TYPES.register(
			"cd_punch_misshaping", key -> new StandEffectType<>(key, CrazyDMisshapingPunchEffect::new));
//
//	public static final DeferredHolder<StandEffectType<?>, StandEffectType<CrazyDAngeloRockPunchEffect>> EFFECT_CD_PUNCH_ANGELO_ROCK = STAND_EFFECT_TYPES.register(
//			"cd_punch_angelo_rock", key -> new StandEffectType<>(key, CrazyDAngeloRockPunchEffect::new));
	
	public static final Supplier<StandEffectType<?>> _PLACEHOLDER_CD_TURN_INTO_ANGELO_ROCK_EFFECT = () -> null;



	public static final DeferredHolder<AbilityType<?>, AbilityType<Ability>> _PLACEHOLDER = ABILITY_TYPES.register(
			"placeholder", key -> new AbilityType<>(key, Ability::new));
}
