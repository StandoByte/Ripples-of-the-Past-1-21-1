package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopLevelTracker;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeStopInvadeAbility extends Ability {

	public TimeStopInvadeAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		canUseInStoppedTime = true;
	}
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(context);
		if (standPower != null) {
			LivingEntity user = standPower.getUser();
			if (user != null) {
				// if the player is not stopping time && there is a time stop instance in the area
				if (!standPower.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).isPresent()
						&& TimeStopLevelTracker.timeStops(standPower.getUser().level()).anyMatch(timeStop -> timeStop.isInRange(user.blockPosition()))) {
					abilities.replaceOtherAbilityWith(standPower, "time_stop", this);
				}
			}
		}
		
		return null;
	}
	
	// FIXME temporary
	@Override
	@Deprecated
	public boolean isAbilityUnlocked(Power<?> context) {
		return isSkillUnlocked(context, "time_stop");
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		StandPower power = PowerClass.STAND.cast(context);
		if (power != null && power.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).isPresent()) {
			return ConditionCheck.NEGATIVE;
		}
		return super.checkSpecificConditions(context);
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				// TODO a variation which stops as soon as the other time stop instances stop (Star Platinum asspull path)
				TimeStopEffect timeStop = ModStandAbilities.EFFECT_TIME_STOP.get().create(level);
				standPower.userStandEffects.addEffect(timeStop);
			}
		}
	}

}
