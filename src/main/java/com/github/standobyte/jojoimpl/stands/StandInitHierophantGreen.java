package com.github.standobyte.jojoimpl.stands;

import static com.github.standobyte.jojo.init.power.ModStands.SWITCH_SPECIAL;
import static com.github.standobyte.jojo.init.power.ModStands.USE_SPECIAL;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;

import net.minecraft.resources.ResourceLocation;

public class StandInitHierophantGreen {

	@ApiStatus.Internal
	public static EntityStandType create(ResourceLocation id) {
		return new EntityStandType(
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
			.discTooltipExperimental();
	}
}
