package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeStopInvadeAbility extends Ability {

	public TimeStopInvadeAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		canUseInStoppedTime = true;
	}
	
	public static boolean canInvadeTimeStop(StandPower userPower) {
		LivingEntity user = userPower.getUser();
		return user != null 
				// if the player is not stopping time && there is someone else's time stop instance in the area
				&& !userPower.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).isPresent()
				&& TimeStopEffect.getIsInsideTimeStop(user);
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
		// TODO a variation which stops as soon as the other time stop instances stop (Star Platinum asspull path)
		TimeStopAbility.addTimeStopEffect(user, this, TimeStopAbility.getDuration(user), true);
	}

}
