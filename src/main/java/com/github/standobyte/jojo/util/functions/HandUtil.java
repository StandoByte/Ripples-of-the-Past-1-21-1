package com.github.standobyte.jojo.util.functions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class HandUtil {

	public static InteractionHand getHand(LivingEntity entity, HumanoidArm side) {
		return entity.getMainArm() == side ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}
}
