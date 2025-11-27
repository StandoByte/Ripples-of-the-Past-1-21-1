package com.github.standobyte.jojo.init.power;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputUseVanillaMapping;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// XXX test the stand datapack configs
// XXX add a way to ban hardcoded stands
public class ModStands {
	public static final DeferredRegister<StandType> DEFAULT_STANDS = DeferredRegister.create(JojoRegistries.DEFAULT_STANDS_REG, JojoMod.MOD_ID);
	
	public static InputUseVanillaMapping USE_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.use_special_ability");
	public static InputUseVanillaMapping SWITCH_SPECIAL = new InputUseVanillaMapping("jojo_ripples.key.ability_hotbar");
	
	public static final DeferredHolder<StandType, EntityStandType> STAR_PLATINUM = DEFAULT_STANDS.register(
			"star_platinum", id -> 
			new EntityStandType(
					new StandStats.Builder()
					.power(18.5)
					.speed(19)
					.range(2, 10)
					.durability(20)
					.precision(20)
					.build(),

					new MovesetBuilder()
					
					// has a higher priority than regular item usage (added in addHumanoidStandStuff()) or charged heavy
					.addAbility("bearing_shot", ModStandAbilities.BEARING_SHOT)
					.withBind(InputMethod.HOLD, InputKey.RMB)
					
					.addHumanoidStandStuff()
					
					.addAbility("punch", ModStandAbilities.PUNCH)
					.withBind(InputMethod.CLICK, InputKey.LMB)
					
					// TODO refactor sub-punches initialization
					.addAbility("punch2", ModStandAbilities.PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("punch3", ModStandAbilities.PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("punch4", ModStandAbilities.PUNCH, punch -> {
						punch.isSubAbility = true;
						punch.setDefaultPhaseLength(ActionPhase.WINDUP, 5);
					})
					
					.addAbility("barrage", ModStandAbilities.BARRAGE)
					.withBind(InputMethod.HOLD, InputKey.LMB)

					.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
					.withBind(InputMethod.CLICK, InputKey.RMB)
					.addAbility("heavy_punch2", ModStandAbilities.HEAVY_PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("finisher_uppercut", ModStandAbilities.HEAVY_PUNCH, punch -> punch.isSubAbility = true)
					
					.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)
					.withBind(InputMethod.HOLD, InputKey.RMB)
					
					.addAbility("grab",ModStandAbilities.GRAB)
					.withBind(InputMethod.CLICK, InputKey.RMB.withModifier(InputKey.Modifier.CONTROL))
					
					.addAbility("grab_release", ModStandAbilities.GRAB_RELEASE)
					.withBind(InputMethod.CLICK, InputKey.Q)
					
					.addAbility("grab_throw", ModStandAbilities.GRAB_THROW)
					.withBind(InputMethod.HOLD, InputKey.RMB)
					
					.addAbility("grab_punch", ModStandAbilities.GRAB_PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("grab_barrage", ModStandAbilities.GRAB_BARRAGE, punch -> punch.isSubAbility = true)
					.addAbility("grab_uppercut", ModStandAbilities.GRAB_HEAVY_PUNCH, punch -> {
						punch.isSubAbility = true;
						punch.verticalKnockback = true;
					})
					
//					.addAbility("grab_ground_slam", ModStandAbilities.HEAVY_PUNCH)
//					.addAbility("grab_terrain", ModStandAbilities.GRAB_TERRAIN)
//					.addAbility("terrain_throw", ModStandAbilities.GRAB_TERRAIN_THROW)
					
//					.addAbility("guard", ModStandAbilities.GUARD)
					
//					.addAbility("leap", ModStandAbilities.STAND_LEAP)

//					.addAbility("uppercut_ground_throw", ModStandAbilities.HEAVY_PUNCH)
					

					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
					
//					.addAbility("enhanced_eyesight", ModStandAbilities.SP_EYESIGHT)
//					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("star_finger", ModStandAbilities.SP_STAR_FINGER)
					.inHotbar(0, InputMethod.CLICK)
					
//					.addAbility("star_finger_swipe", ModStandAbilities.SP_STAR_FINGER_SWIPE)
//					.inHotbarSlotVariation("star_finger", InputKey.Modifier.CONTROL, InputMethod.CLICK)

                    .addAbility("inhale", ModStandAbilities.SP_INHALE)
					.inHotbar(0, InputMethod.HOLD)
					
//					.addAbility("time_stop", ModStandAbilities.TIME_STOP)
//					.inHotbar(0, InputMethod.CLICK)
					
					
					.addSkill(StandUnlockableSkill.startingAbility("punch"))
					.addSkill(StandUnlockableSkill.startingAbility("barrage"))
					.addSkill(StandUnlockableSkill.startingAbility("heavy_punch"))
					.addSkill(StandUnlockableSkill.startingAbility("uppercut").prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.startingAbility("heavy_charged").prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.unlockableAbility("ground_slam", 1).prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.startingAbility("grab"))
					.addSkill(StandUnlockableSkill.tiedToMainSkill("block_toss", "grab").withAbility("block_toss"))
					.addSkill(StandUnlockableSkill.unlockableAbility("grab_throw", 1).prerequisiteSkill("grab"))
					.addSkill(StandUnlockableSkill.unlockableAbility("grab_terrain", 1).withAbility("terrain_throw").prerequisiteSkill("grab"))
					.addSkill(StandUnlockableSkill.unlockableAbility("uppercut_ground_throw", 1).prerequisiteSkill("uppercut", "ground_slam"))
					
					.addSkill(StandUnlockableSkill.startingAbility("enhanced_eyesight"))
					.addSkill(StandUnlockableSkill.unlockableAbility("star_finger", 1)/*.withAbility("star_finger_swipe")*/)
					.addSkill(StandUnlockableSkill.unlockableAbility("inhale", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("time_stop", 1))

					.addHumanoidStandSkills()

					, id)
			.discTooltipWIP()
			 /* This is to make it appear first in the list of Stardust Crusaders Stands in the creative tab, as the protagonist's Stand */
			.init(stand -> stand.discStoryPartPriority = 0));
	
	
	
	public static final DeferredHolder<StandType, EntityStandType> CRAZY_DIAMOND = DEFAULT_STANDS.register(
			"crazy_diamond", id -> 
			new EntityStandType(
					new StandStats.Builder()
					.power(17)
					.speed(16.5)
					.range(2, 4)
					.durability(13)
					.precision(12)
					.build(),

					new MovesetBuilder()
					
					.addHumanoidStandStuff()
					
					.addAbility("repair_item", ModStandAbilities.CD_REPAIR_ITEM)
					.withBind(InputMethod.HOLD, InputKey.C)
					

					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
					
					.addAbility("block_bullet", ModStandAbilities.CD_BLOCK_BULLET)
					.inHotbar(0, InputMethod.CLICK)
					
					.addAbility("blood_cutter", ModStandAbilities.CD_BLOOD_CUTTER)
					.inHotbar(0, InputMethod.CLICK)
					
					
					.addSkill(StandUnlockableSkill.startingAbility("punch"))
					.addSkill(StandUnlockableSkill.startingAbility("barrage"))
					.addSkill(StandUnlockableSkill.startingAbility("heavy_punch"))
					.addSkill(StandUnlockableSkill.startingAbility("finisher").prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.unlockableAbility("disfiguring_punch", 1).prerequisiteSkill("heal", "finisher"))
					.addSkill(StandUnlockableSkill.startingAbility("heavy_charged").prerequisiteSkill("heavy_punch"))
					.addSkill(StandUnlockableSkill.unlockableAbility("leave_object", 1).prerequisiteSkill("heal", "heavy_charged"))
					.addSkill(StandUnlockableSkill.startingAbility("grab"))
					.addSkill(StandUnlockableSkill.tiedToMainSkill("block_toss", "grab"))
					
					.addSkill(StandUnlockableSkill.startingAbility("repair_item"))
					.addSkill(StandUnlockableSkill.unlockableAbility("heal", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("revert_state", 1).prerequisiteSkill("repair_item"))
					.addSkill(StandUnlockableSkill.tiedToMainSkill("uncraft", "revert_state").withAbility("uncraft"))
					.addSkill(StandUnlockableSkill.unlockableAbility("restore_terrain", 1))
					.addSkill(StandUnlockableSkill.unlockableAbility("create_wall", 1).prerequisiteSkill("restore_terrain"))
					.addSkill(StandUnlockableSkill.unlockableAbility("fuse_with_rock", 1).prerequisiteSkill("finisher_misshape", "restore_terrain"))
					.addSkill(StandUnlockableSkill.unlockableAbility("block_anchor", 1).withAbility("block_anchor_move"))
					.addSkill(StandUnlockableSkill.unlockableAbility("block_bullet", 1).withAbility("blood_cutter"))
					.addSkill(StandUnlockableSkill.tiedToMainSkill("blood_cutter", "block_bullet").withAbility("blood_cutter"))

					.addHumanoidStandSkills()

					, id)
			.discTooltipWIP()
			.init(stand -> stand.discStoryPartPriority = 0));
	
	
	
	public static final DeferredHolder<StandType, EntityStandType> HIEROPHANT_GREEN = DEFAULT_STANDS.register(
			"hierophant_green", id -> 
			new EntityStandType(
					new StandStats.Builder()
					.power(9)
					.speed(12)
					.range(50, 100)
					.durability(10)
					.precision(10)
					.build(),

					new MovesetBuilder()
					
					.addHumanoidStandStuff()
					

					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
					
					.addAbility("puppet", ModStandAbilities.HG_PUPPET)
					.inHotbar(0, InputMethod.CLICK)
					
					
					.addSkill(StandUnlockableSkill.unlockableAbility("puppet", 1))

					, id)
			.discTooltipExperimental());
}
