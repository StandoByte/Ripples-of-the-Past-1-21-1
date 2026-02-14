package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectType;

import net.minecraft.world.entity.LivingEntity;

public class CrazyDAngeloRockPunchEffect extends StandEffectInstance {

	public CrazyDAngeloRockPunchEffect(StandEffectType<?> effectType) {
		super(effectType);
		isFromStandAction = true;
	}
	
	@Override
	protected void start() {}

	@Override
	protected void tick() {
		if (!level.isClientSide()) {
			if (standAction == null) {
				remove();
				return;
			}
			else if (standAction.getActionTicksLeft() <= 1 && standAction.punchedTarget != null && standAction.punchedTarget.getEntity() instanceof LivingEntity target) {
				JojoMod.LOGGER.debug("{}", target.getDisplayName().getString());
				remove();
			}
			else if (standAction.isOver()) {
				remove();
			}
		}
	}

	@Override
	protected void stop() {}

}
