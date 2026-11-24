package com.github.standobyte.jojoimpl.stands;

import static com.github.standobyte.jojo.init.power.ModStands.SWITCH_SPECIAL;
import static com.github.standobyte.jojo.init.power.ModStands.USE_SPECIAL;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ui.utils.Orientation;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;

import net.minecraft.resources.ResourceLocation;

public class StandInitCrazyDiamond {

	@ApiStatus.Internal
	public static EntityStandType create(ResourceLocation id) {
		return new EntityStandType(
				new StandStats.Builder()
				.power(17)
				.speed(16.5)
				.range(2, 4)
				.durability(13)
				.precision(12)
				.build(),

				new MovesetBuilder()

				.addHumanoidStandStuff()

				.addAbility("punch", ModStandAbilities.PUNCH)
				.addAbility("punch2", ModStandAbilities.PUNCH)
				.addAbility("punch3", ModStandAbilities.PUNCH)
				.addAbility("punch4", ModStandAbilities.PUNCH)
				.addAbility("barrage", ModStandAbilities.BARRAGE)
				.addAbility("heavy_punch", ModStandAbilities.HEAVY_PUNCH)
				.addAbility("finisher", ModStandAbilities.HEAVY_PUNCH, punch -> {
					punch.initIsFinisher();
				})
				.addAbility("heavy_charged", ModStandAbilities.HEAVY_CHARGED)

				.addAbility("grab", ModStandAbilities.GRAB)

				.addAbility("grab_punch", ModStandAbilities.PUNCH, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_barrage", ModStandAbilities.BARRAGE, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_heavy_punch", ModStandAbilities.HEAVY_PUNCH, punch -> {
					punch.initIsGrabVariation();
				})
				.addAbility("grab_finisher", ModStandAbilities.HEAVY_PUNCH, punch -> {
					punch.initIsGrabVariation();
					punch.initIsFinisher("grab_heavy_punch");
				})

				.addAbility("guard", ModStandAbilities.GUARD)
//				.addAbility("leap", ModStandAbilities.STAND_LEAP)
				.addAbility("block_toss", ModStandAbilities.BLOCK_TOSS)
				.addAbility("bearing_shot", ModStandAbilities.BEARING_SHOT)

				.addAbility("leave_object", ModStandAbilities.CD_LEAVE_OBJECT_ON_PUNCH)
				.addAbility("mishealing_punch", ModStandAbilities.CD_MISHEAL_ON_PUNCH)
				.addAbility("fuse_with_rock", ModStandAbilities.CD_ANGELO_ROCK_ON_PUNCH)

				.addAbility("repair_item", ModStandAbilities.CD_REPAIR_ITEM)
				.addAbility("uncraft", ModStandAbilities.CD_UNCRAFT_ITEM)
				.addAbility("heal", ModStandAbilities.CD_HEAL)
				.addAbility("revert_state", ModStandAbilities.CD_REVERT_STATE)
				.addAbility("restore_terrain", ModStandAbilities.CD_RESTORE_TERRAIN)
				.addAbility("block_anchor", ModStandAbilities.CD_ANCHOR_MOVE)
				.addAbility("create_wall", ModStandAbilities.CD_WALL_FROM_TERRAIN)
				.addAbility("block_bullet", ModStandAbilities.CD_BLOCK_BULLET)
				.addAbility("blood_cutter", ModStandAbilities.CD_BLOOD_CUTTER)


				.makeControlScheme("hotbar")
					.bind("bearing_shot", InputMethod.HOLD, InputKey.RMB)
					.bind("punch", InputMethod.CLICK, InputKey.LMB)
					.bind("barrage", InputMethod.HOLD, InputKey.LMB)
					.bind("heavy_punch", InputMethod.CLICK, InputKey.RMB)
					.bind("heavy_charged", InputMethod.HOLD, InputKey.RMB)
					.bind("guard", InputMethod.CLICK, InputKey.MMB)
					.bind("grab", InputMethod.CLICK, InputKey.RMB.withModifier(InputKey.Modifier.CONTROL))
					.bind("block_toss", InputMethod.HOLD, InputKey.LMB)
	
					.bind("repair_item", InputMethod.HOLD, InputKey.C)
					.bind("uncraft", InputMethod.HOLD, InputKey.I)
	
					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
					.addToHotbar("blood_cutter", 0, InputMethod.CLICK)
					.addToHotbar("block_bullet", 0, InputMethod.CLICK)
					.addToHotbar("heal", 0, InputMethod.HOLD)
//					.addHotbarSlotVariation("revert_state", "heal", InputKey.Modifier.CONTROL, InputMethod.HOLD)
					.addToHotbar("restore_terrain", 0, InputMethod.HOLD)
					.addHotbarSlotVariation("create_wall", "restore_terrain", InputKey.Modifier.CONTROL, InputMethod.CLICK)
				.finalizeControlScheme()


				.makeControlScheme("keybinds")
					.bind("bearing_shot", InputMethod.HOLD, InputKey.RMB)
					.bind("punch", InputMethod.CLICK, InputKey.LMB)
					.bind("barrage", InputMethod.HOLD, InputKey.LMB)
					.bind("heavy_punch", InputMethod.CLICK, InputKey.RMB)
					.bind("heavy_charged", InputMethod.HOLD, InputKey.RMB)
					.bind("guard", InputMethod.CLICK, InputKey.MMB)
					.bind("grab", InputMethod.CLICK, InputKey.RMB.withModifier(InputKey.Modifier.CONTROL))
					.bind("block_toss", InputMethod.HOLD, InputKey.LMB)
	
					.bind("repair_item", InputMethod.HOLD, InputKey.C)
					.bind("uncraft", InputMethod.HOLD, InputKey.I)
	
					.bind("blood_cutter", InputMethod.CLICK, InputKey.Z)
					.bind("block_bullet", InputMethod.CLICK, InputKey.X)
					.bind("heal", InputMethod.HOLD, InputKey.C)
//					.bind("revert_state", InputMethod.HOLD, InputKey.C.withModifier(InputKey.Modifier.CONTROL))
					.bind("restore_terrain", InputMethod.HOLD, InputKey.V)
					.bind("create_wall", InputMethod.CLICK, InputKey.V.withModifier(InputKey.Modifier.CONTROL))
				.finalizeControlScheme()


				.addSkill(StandUnlockableSkill.startingAbility("punch"))
				.addSkill(StandUnlockableSkill.startingAbility("barrage"))
				.addSkill(StandUnlockableSkill.startingAbility("heavy_punch"))
				.addSkill(StandUnlockableSkill.unlockableAbility("hit_armor_fix", 200)
						.setNotYetImplemented()
						.prerequisiteSkill("repair_item"))
				.addSkill(StandUnlockableSkill.startingAbility("finisher"))
				.addSkill(StandUnlockableSkill.unlockableAbility("mishealing_punch", 200)
						.setNotYetImplemented()
						.prerequisiteSkill("heal"))
				.addSkill(StandUnlockableSkill.unlockableAbility("fuse_with_rock", 200)
						.setNotYetImplemented()
						.prerequisiteSkill("heal", "restore_terrain"))
				.addSkill(StandUnlockableSkill.startingAbility("heavy_charged"))
				.addSkill(StandUnlockableSkill.unlockableAbility("leave_object", 200)
						.setNotYetImplemented()
						.prerequisiteSkill("heal"))

				.addSkill(StandUnlockableSkill.startingAbility("grab")
						.setIncomplete())
				.addSkill(StandUnlockableSkill.startingAbility("block_toss")
						.setIncomplete())
				
				.addSkill(StandUnlockableSkill.startingAbility("guard")
						.setIncomplete())
				
				.addManualControlSkill()
				.addSkill(new StandUnlockableSkill("item_use").setIsStartingSkill())
				
				.addSkill(StandUnlockableSkill.unlockableAbility("leap", 250)
						.setNotYetImplemented())
				.addSkill(StandUnlockableSkill.startingAbility("ledge_grab")
						.setNotYetImplemented())
				
				
				.addSkill(StandUnlockableSkill.startingAbility("repair_item"))
				.addSkill(StandUnlockableSkill.unlockableAbility("uncraft", 100)
						.prerequisiteSkill("repair_item"))
				.addSkill(StandUnlockableSkill.unlockableAbility("block_bullet", 150)
						.prerequisiteSkill("uncraft"))

				.addSkill(StandUnlockableSkill.unlockableAbility("heal", 100))
				.addSkill(StandUnlockableSkill.unlockableAbility("fix_broken_entity", 0)
						.setNotYetImplemented()
						.prerequisiteSkill("heal"))

				.addSkill(StandUnlockableSkill.unlockableAbility("restore_terrain", 150))
				.addSkill(StandUnlockableSkill.unlockableAbility("block_anchor", 0)
						.prerequisiteSkill("restore_terrain"))
				.addSkill(StandUnlockableSkill.unlockableAbility("create_wall", 50)
						.setNotYetImplemented()
						.prerequisiteSkill("restore_terrain"))
				
				.addSkill(StandUnlockableSkill.unlockableAbility("revert_state", 100)
						.setNotYetImplemented()
						.prerequisiteSkill("restore_terrain", "heal"))

				.addSkill(StandUnlockableSkill.unlockableAbility("blood_cutter", 150))
				
				.generateSkillTreeUI(layout -> {
					// listen, if it works - it works
					layout.setPosition("repair_item", 		 98,  95);
					layout.setPosition("hit_armor_fix", 	 85,  63);
					layout.setPosition("uncraft", 			112,  63);
					layout.setPosition("block_bullet", 		142,  63);
					layout.setConnectivityDirection("repair_item", "hit_armor_fix", Orientation.VERTICAL);
					layout.setConnectivityDirection("repair_item", "uncraft", Orientation.VERTICAL);
					
					layout.setPosition("heal", 				 78, 134);
					layout.setPosition("fix_broken_entity",  46, 107);
					layout.setPosition("mishealing_punch", 	 46, 134);
					layout.setPosition("leave_object", 		 46, 161);
					
					layout.setPosition("restore_terrain", 	118, 134);
					layout.setPosition("block_anchor", 		150, 121);
					layout.setPosition("create_wall", 		150, 148);
					
					layout.setPosition("fuse_with_rock", 	 84, 176);
					layout.setPosition("revert_state", 		112, 176);
					layout.setConnectivityDirection("heal", "fuse_with_rock", Orientation.VERTICAL);
					layout.setConnectivityDirection("heal", "revert_state", Orientation.VERTICAL);
					layout.setConnectivityDirection("restore_terrain", "fuse_with_rock", Orientation.VERTICAL);
					layout.setConnectivityDirection("restore_terrain", "revert_state", Orientation.VERTICAL);
					
					layout.setPosition("blood_cutter", 		142,  93);
				})

				, id)
			.discTooltipWIP()
			.init(stand -> stand.discStoryPartPriority = 0);
	}
}
