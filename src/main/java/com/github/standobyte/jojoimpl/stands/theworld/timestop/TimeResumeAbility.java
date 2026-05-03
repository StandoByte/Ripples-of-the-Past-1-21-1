package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeResumeAbility extends Ability {

	public TimeResumeAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
	}
	
	@Override
	public boolean isAbilityAvailable(Power<?> context) {
		if (super.isAbilityAvailable(context)) {
			StandPower power = PowerClass.STAND.cast(context);
			if (power != null && power.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).isPresent()) {
				return true;
			}
		}
		
		return false;
	}
	
	// FIXME (stand skills) make UnlockableSkill#unlocksAbilities handle this instead (this is a temporary solution)
	@Override
	@Deprecated
	public boolean isAbilityUnlocked(Power<?> context) {
		return isSkillUnlocked(context, "time_stop");
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				standPower.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).forEach(
						timeStop -> timeStop.remove());
			}
		}
	}

}
