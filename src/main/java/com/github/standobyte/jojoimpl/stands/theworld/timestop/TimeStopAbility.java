package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import java.util.stream.Stream;

import com.github.standobyte.jojo.client.ui.hud_power.WindupIndicator;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeStopAbility extends EntityActionAbility {

	public TimeStopAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, TimeStopWindupAction::new);
		canUseInStoppedTime = true;
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, 40);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		StandPower power = PowerClass.STAND.cast(context);
		// if already stopping time, gray out the ability
		if (power != null && power.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).isPresent()) {
			return ConditionCheck.NEGATIVE;
		}
		return super.checkSpecificConditions(context);
	}
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null && TimeStopInvadeAbility.canInvadeTimeStop(standPower)) {
				// instantly give the time stop effect when invading
				bufferingState.isActionSuccess = true;
				addTimeStopEffect(user, getDuration(user), false);
			}
			else {
				// set the action with the windup (EntityActionAbility logic)
				return super.onKeyPress(level, user, extraClientInput, 
						inputMethod, clickHoldResolveTime, bufferingState);
			}
		}
		
		return null;
	}
	
	@Override
	public WindupIndicator cl_windupIndicator(LivingEntity clientPlayer, WindupIndicator indicator, float partialTick) {
		if (TimeStopEffect.getIsInsideTimeStop(clientPlayer)) {
			// removes the windup indicator from the ability in the HUD
			return null;
		}
		return super.cl_windupIndicator(clientPlayer, indicator, partialTick);
	}
	
	public static int getDuration(LivingEntity user) {
		return 100;
	}
	
	
	public static class TimeStopWindupAction extends EntityActionInstance {

		public TimeStopWindupAction(EntityActionType ability) {
			super(ability);
			userWalkSpeed = 0;
		}
		
		@Override
		public void actionPerformStart() {
			LivingEntity user = performer;
			addTimeStopEffect(user, getDuration(user), false);
		}
		
	}
	
	
	public static void addTimeStopEffect(LivingEntity user, int duration, boolean stopWhenOtherTSEnds) {
		Level level = user.level();
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				// should be empty, but just in case
				Stream<TimeStopEffect> oldEffects = standPower.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_TIME_STOP.get());
				TimeStopEffect timeStop = ModStandAbilities.EFFECT_TIME_STOP.get().create(level);
				standPower.userStandEffects.addEffect(timeStop);
				oldEffects.forEach(effect -> {
					effect.remove();
				});
			}
		}
	}

}
