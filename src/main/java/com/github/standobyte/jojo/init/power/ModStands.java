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

// XXX (data-driven stands) test the stand datapack configs
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
					
					.addHumanoidStandStuff()
					
					.addAbility("punch", ModStandAbilities.PUNCH)
					
					// FIXME refactor sub-punches initialization
					.addAbility("punch2", ModStandAbilities.PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("punch3", ModStandAbilities.PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("punch4", ModStandAbilities.PUNCH, punch -> {
						punch.isSubAbility = true;
						punch.setDefaultPhaseLength(ActionPhase.WINDUP, 5);
					})
					
					.addAbility("barrage", ModStandAbilities.BARRAGE)

					.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
					.addAbility("finisher_uppercut", ModStandAbilities.HEAVY_PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)
//					.addAbility("ground_slam", ModStandAbilities.HEAVY_PUNCH)
					
					.addAbility("grab",ModStandAbilities.GRAB)
					
					.addAbility("grab_throw", ModStandAbilities.GRAB_THROW)
					.addAbility("grab_punch", ModStandAbilities.GRAB_PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("grab_barrage", ModStandAbilities.GRAB_BARRAGE, punch -> punch.isSubAbility = true)
//					.addAbility("grab_heavy_punch", ModStandAbilities.GRAB_HEAVY_PUNCH, punch -> punch.isSubAbility = true)
					.addAbility("grab_uppercut", ModStandAbilities.GRAB_HEAVY_PUNCH, punch -> {
						punch.isSubAbility = true;
						punch.verticalKnockback = true;
					})
//					.addAbility("grab_ground_slam", ModStandAbilities.HEAVY_PUNCH)
					
//					.addAbility("grab_terrain", ModStandAbilities.GRAB_TERRAIN)
//					.addAbility("terrain_throw", ModStandAbilities.GRAB_TERRAIN_THROW)
//					.addAbility("uppercut_ground_throw", ModStandAbilities.HEAVY_PUNCH)
					
//					.addAbility("guard", ModStandAbilities.GUARD)
//					.addAbility("leap", ModStandAbilities.STAND_LEAP)

//					.addAbility("enhanced_eyesight", ModStandAbilities.SP_EYESIGHT)
					.addAbility("star_finger", ModStandAbilities.SP_STAR_FINGER)
//					.addAbility("star_finger_swipe", ModStandAbilities.SP_STAR_FINGER_SWIPE)
					.addAbility("inhale", ModStandAbilities.SP_INHALE)
//					.addAbility("time_stop", ModStandAbilities.TIME_STOP)
					
					
					.makeControlScheme("hotbar")
						.bind("bearing_shot", InputMethod.HOLD, InputKey.RMB)
						.bind("punch", InputMethod.CLICK, InputKey.LMB)
						.bind("barrage", InputMethod.HOLD, InputKey.LMB)
						.bind("heavy_punch", InputMethod.CLICK, InputKey.RMB)
						.bind("heavy_charged", InputMethod.HOLD, InputKey.RMB)
						.bind("grab", InputMethod.CLICK, InputKey.RMB.withModifier(InputKey.Modifier.CONTROL))
						.bind("grab_throw", InputMethod.HOLD, InputKey.RMB)
						
						.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
//						.addToHotbar("enhanced_eyesight", 0, InputMethod.CLICK)
						.addToHotbar("star_finger", 0, InputMethod.CLICK)
//						.addToHotbarSlotVariation("star_finger_swipe", "star_finger", InputKey.Modifier.CONTROL, InputMethod.CLICK)
						.addToHotbar("inhale", 0, InputMethod.HOLD)
//						.addToHotbar("time_stop", 0, InputMethod.HOLD)
					.finalizeControlScheme()
					
					
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
					
//					.addAbility("punch", ModStandAbilities.PUNCH)
//					.addAbility("barrage", ModStandAbilities.BARRAGE)
//					.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
//					.addAbility("finisher", ModStandAbilities.HEAVY_PUNCH, punch -> punch.isSubAbility = true)
//					.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)
					
//					.addAbility("grab",ModStandAbilities.GRAB)
					
//					.addAbility("grab_throw", ModStandAbilities.GRAB_THROW)
//					.addAbility("grab_punch", ModStandAbilities.GRAB_PUNCH, punch -> punch.isSubAbility = true)
//					.addAbility("grab_barrage", ModStandAbilities.GRAB_BARRAGE, punch -> punch.isSubAbility = true)
//					.addAbility("grab_heavy_punch", ModStandAbilities.GRAB_HEAVY_PUNCH, punch -> punch.isSubAbility = true)
//					.addAbility("grab_finisher", ModStandAbilities.GRAB_HEAVY_PUNCH, punch -> punch.isSubAbility = true)
					
//					.addAbility("guard", ModStandAbilities.GUARD)
//					.addAbility("leap", ModStandAbilities.STAND_LEAP)
					
//					.addAbility("leave_object", ModStandAbilities._PLACEHOLDER)
//					.addAbility("disfiguring_punch", ModStandAbilities._PLACEHOLDER)
//					.addAbility("fuse_with_rock", ModStandAbilities._PLACEHOLDER)
					
					.addAbility("repair_item", ModStandAbilities.CD_REPAIR_ITEM)
//					.addAbility("uncraft", ModStandAbilities.CD_REPAIR_ITEM)
//					.addAbility("heal", ModStandAbilities._PLACEHOLDER)
//					.addAbility("revert_state", ModStandAbilities._PLACEHOLDER)
//					.addAbility("restore_terrain", ModStandAbilities._PLACEHOLDER)
//					.addAbility("create_wall", ModStandAbilities._PLACEHOLDER)
					.addAbility("block_bullet", ModStandAbilities.CD_BLOCK_BULLET)
					.addAbility("blood_cutter", ModStandAbilities.CD_BLOOD_CUTTER)
					
					
//					.makeControlScheme("keybinds")
//						.bind("repair_item", InputMethod.HOLD, InputKey.C)
//						
//						.bind("blood_cutter", InputMethod.CLICK, InputKey.Z)
//						.bind("block_bullet", InputMethod.CLICK, InputKey.X)
//						.bind("heal", InputMethod.HOLD, InputKey.C)
//						.bind("revert_state", InputMethod.HOLD, InputKey.C.withModifier(InputKey.Modifier.CONTROL))
//						.bind("restore_terrain", InputMethod.HOLD, InputKey.V)
//						.bind("create_wall", InputMethod.CLICK, InputKey.V.withModifier(InputKey.Modifier.CONTROL))
//					.finalizeControlScheme()
					
					
					.makeControlScheme("hotbar")
						.bind("repair_item", InputMethod.HOLD, InputKey.C)
						
						.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
						.addToHotbar("blood_cutter", 0, InputMethod.CLICK)
						.addToHotbar("block_bullet", 0, InputMethod.CLICK)
						.addToHotbar("heal", 0, InputMethod.HOLD)
						.addHotbarSlotVariation("revert_state", "heal", InputKey.Modifier.CONTROL, InputMethod.HOLD)
						.addToHotbar("restore_terrain", 0, InputMethod.HOLD)
						.addHotbarSlotVariation("create_wall", "restore_terrain", InputKey.Modifier.CONTROL, InputMethod.CLICK)
					.finalizeControlScheme()
					
					
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
					.addSkill(StandUnlockableSkill.tiedToMainSkill("block_anchor", "restore_terrain"))
					.addSkill(StandUnlockableSkill.unlockableAbility("create_wall", 1).prerequisiteSkill("restore_terrain"))
					.addSkill(StandUnlockableSkill.unlockableAbility("fuse_with_rock", 1).prerequisiteSkill("finisher_misshape", "restore_terrain"))
					
					
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
					
					.addAbility("puppet", ModStandAbilities.HG_PUPPET)
					
					
					.makeControlScheme("hotbar")
						.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
						.addToHotbar("puppet", 0, InputMethod.CLICK)
					.finalizeControlScheme()
					
					
					.addSkill(StandUnlockableSkill.unlockableAbility("puppet", 1))

					, id)
			.discTooltipExperimental());
}
