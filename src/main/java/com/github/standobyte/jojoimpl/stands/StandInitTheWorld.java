package com.github.standobyte.jojoimpl.stands;

import static com.github.standobyte.jojo.init.power.ModStands.SWITCH_SPECIAL;
import static com.github.standobyte.jojo.init.power.ModStands.USE_SPECIAL;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.controls.InputKey.Modifier;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.StandUnlockableSkill;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;

import net.minecraft.resources.ResourceLocation;

public class StandInitTheWorld {

	@ApiStatus.Internal
	public static EntityStandType create(ResourceLocation id) {
		return new EntityStandType(
				new StandStats.Builder()
				.power(19)
				.speed(18.5)
				.range(5, 10)
				.durability(20)
				.precision(12)
				.build(),

				new MovesetBuilder()

				.addHumanoidStandStuff()

				.addAbility("time_stop", ModStandAbilities.TIME_STOP)
				.addAbility("time_resume", ModStandAbilities.TIME_RESUME)


				.makeControlScheme("hotbar")
					.makeHotbar(0, USE_SPECIAL, SWITCH_SPECIAL)
					.addToHotbar("time_stop", 0, InputMethod.HOLD)
					.addHotbarSlotVariation("time_resume", "time_stop", Modifier.CONTROL, InputMethod.CLICK)
				.finalizeControlScheme()


				.addSkill(StandUnlockableSkill.unlockableAbility("time_stop", 500).withAbility("time_resume").setIncomplete())

				, id)
			.discTooltipExperimental();
	}
}
