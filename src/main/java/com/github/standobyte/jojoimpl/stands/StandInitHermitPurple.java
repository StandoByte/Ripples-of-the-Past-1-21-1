package com.github.standobyte.jojoimpl.stands;

import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;

import net.minecraft.resources.ResourceLocation;

public class StandInitHermitPurple {
	
	public static EntityStandType create(ResourceLocation id) {
		return new EntityStandType(
				new StandStats.Builder()
				.power(10)
				.speed(10)
				.range(10, 10)
				.durability(10)
				.precision(10)
				.build(),

				new MovesetBuilder()

				, id)
			.discTooltipExperimental()
			.init(stand -> stand.discStoryPartPriority = 4);
	}
}
