package com.github.standobyte.jojoimpl.stands;

import static com.github.standobyte.jojo.init.power.ModStands.SWITCH_SPECIAL;
import static com.github.standobyte.jojo.init.power.ModStands.USE_SPECIAL;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey.Modifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;

import net.minecraft.resources.ResourceLocation;

public class StandInitStarPlatinum {

	@ApiStatus.Internal
	public static EntityStandType create(ResourceLocation id) {
		return new EntityStandType(
				new StandStats.Builder()
				.power(18.5)
				.speed(19)
				.range(2, 10)
				.durability(20)
				.precision(20)
				.build(),

				new MovesetBuilder()

				.addHumanoidStandStuff()

				.addAbility("punch", ModStandAbilities.PUNCH)
				.addAbility("punch2", ModStandAbilities.PUNCH)
				.addAbility("punch3", ModStandAbilities.PUNCH)
				.addAbility("punch4", ModStandAbilities.PUNCH, punch -> {
					punch.setDefaultPhaseLength(ActionPhase.WINDUP, 5);
				})

				.addAbility("barrage", ModStandAbilities.BARRAGE)

				.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
				.addAbility("finisher_uppercut", ModStandAbilities.HEAVY_UPPERCUT, punch -> {
					punch.initIsFinisher();
				})
				.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)
//				.addAbility("ground_slam", ModStandAbilities.HEAVY_PUNCH)

				.addAbility("grab",ModStandAbilities.GRAB)

				.addAbility("grab_throw", ModStandAbilities.GRAB_THROW)
				.addAbility("grab_punch", ModStandAbilities.PUNCH, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_barrage", ModStandAbilities.BARRAGE, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_heavy_punch", ModStandAbilities.HEAVY_PUNCH, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_uppercut", ModStandAbilities.HEAVY_UPPERCUT, punch -> {
					punch.initIsGrabVariation();
					punch.initIsFinisher("grab_heavy_punch");
				})
//				.addAbility("grab_ground_slam", ModStandAbilities.HEAVY_PUNCH)

//				.addAbility("grab_terrain", ModStandAbilities.GRAB_TERRAIN)
//				.addAbility("terrain_throw", ModStandAbilities.GRAB_TERRAIN_THROW)
//				.addAbility("uppercut_ground_throw", ModStandAbilities.HEAVY_PUNCH)

//				.addAbility("guard", ModStandAbilities.GUARD)
//				.addAbility("leap", ModStandAbilities.STAND_LEAP)
				.addAbility("bearing_shot", ModStandAbilities.BEARING_SHOT)

//				.addAbility("enhanced_eyesight", ModStandAbilities.SP_EYESIGHT)
				.addAbility("star_finger", ModStandAbilities.SP_STAR_FINGER)
//				.addAbility("star_finger_swipe", ModStandAbilities.SP_STAR_FINGER_SWIPE)
				.addAbility("inhale", ModStandAbilities.SP_INHALE)
				.addAbility("time_stop", ModStandAbilities.TIME_STOP)
				.addAbility("time_resume", ModStandAbilities.TIME_RESUME)


				.makeControlScheme("hotbar")
					.bind("punch", InputMethod.CLICK, InputKey.LMB)
					.bind("barrage", InputMethod.HOLD, InputKey.LMB)
					.bind("heavy_punch", InputMethod.CLICK, InputKey.RMB)
					.bind("heavy_charged", InputMethod.HOLD, InputKey.RMB)
					.bind("grab", InputMethod.CLICK, InputKey.RMB.withModifier(InputKey.Modifier.CONTROL))
					.bind("grab_throw", InputMethod.HOLD, InputKey.RMB)
					.bind("bearing_shot", InputMethod.HOLD, InputKey.RMB)
	
					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
//					.addToHotbar("enhanced_eyesight", 0, InputMethod.CLICK)
					.addToHotbar("star_finger", 0, InputMethod.CLICK)
//					.addToHotbarSlotVariation("star_finger_swipe", "star_finger", InputKey.Modifier.CONTROL, InputMethod.CLICK)
					.addToHotbar("inhale", 0, InputMethod.HOLD)
					.addToHotbar("time_stop", 0, InputMethod.HOLD)
					.addHotbarSlotVariation("time_resume", "time_stop", Modifier.CONTROL, InputMethod.CLICK)
				.finalizeControlScheme()


				.addSkill(StandUnlockableSkill.startingAbility("punch"))
				.addSkill(StandUnlockableSkill.startingAbility("barrage"))
				.addSkill(StandUnlockableSkill.startingAbility("heavy_punch"))
				.addSkill(StandUnlockableSkill.startingAbility("uppercut").prerequisiteSkill("heavy_punch").setIncomplete())
				.addSkill(StandUnlockableSkill.unlockableAbility("ground_slam", 200).setNotYetImplemented().prerequisiteSkill("heavy_punch"))
				.addSkill(StandUnlockableSkill.unlockableAbility("uppercut_ground_throw", 200).setNotYetImplemented().prerequisiteSkill("uppercut", "ground_slam"))
				.addSkill(StandUnlockableSkill.startingAbility("heavy_charged").prerequisiteSkill("heavy_punch"))

				.addSkill(StandUnlockableSkill.startingAbility("grab"))
				.addSkill(StandUnlockableSkill.startingAbility("block_toss").setNotYetImplemented())
				.addSkill(StandUnlockableSkill.unlockableAbility("grab_throw", 100).prerequisiteSkill("grab").setIncomplete())
				.addSkill(StandUnlockableSkill.unlockableAbility("grab_terrain", 150).setNotYetImplemented().withAbility("terrain_throw").prerequisiteSkill("grab"))
				
				.addSkill(StandUnlockableSkill.startingAbility("guard").setNotYetImplemented())

				.addSkill(StandUnlockableSkill.startingAbility("enhanced_eyesight").setNotYetImplemented())
				.addSkill(StandUnlockableSkill.unlockableAbility("star_finger", 250)/*.withAbility("star_finger_swipe")*/)
				.addSkill(StandUnlockableSkill.unlockableAbility("inhale", 150))
				.addSkill(StandUnlockableSkill.unlockableAbility("time_stop", 5000).withAbility("time_resume").setIncomplete())

				.addSkill(StandUnlockableSkill.unlockableAbility("leap", 250).setNotYetImplemented())
				.addSkill(StandUnlockableSkill.startingAbility("ledge_grab").setNotYetImplemented())
				
				.addSkill(StandUnlockableSkill.startingAbility("manual_control"))
				.addSkill(new StandUnlockableSkill("item_use").setIsStartingSkill())

				, id)
			.discTooltipWIP()
			/* This is to make it appear first in the list of Stardust Crusaders Stands in the creative tab, as the protagonist's Stand */
			.init(stand -> stand.discStoryPartPriority = 0);
	}
}
