package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeStopAbility extends Ability {

	public TimeStopAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				TimeStopEffect timeStop = ModStandAbilities.EFFECT_TIME_STOP.get().create(level);
				timeStop.initialPos = user.chunkPosition();
				standPower.userStandEffects.addEffect(timeStop);
			}
		}
	}

}
