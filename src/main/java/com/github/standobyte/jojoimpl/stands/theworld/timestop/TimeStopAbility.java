package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.text.StandSkinComponent;
import com.github.standobyte.jojo.client.ui.hud_power.WindupIndicator;
import com.github.standobyte.jojo.config.core.types.ConfigFloat;
import com.github.standobyte.jojo.config.core.types.ConfigInt;
import com.github.standobyte.jojo.config.stand.AbilityConfigField;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TimeStopAbility extends EntityActionAbility {
	@AbilityConfigField public final ConfigInt standWindupDuration;
	@AbilityConfigField public final ConfigInt timeStopMaxTicks;
	@AbilityConfigField public final ConfigFloat timeStopCooldownPerTick;

	public TimeStopAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, TimeStopWindupAction::new);
		canUseInStoppedTime = true;
		standWindupDuration = new ConfigInt(30).initNonNegative()
				.withOnChangeCallback(ticks -> setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, ticks), true);
		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, standWindupDuration.getDefaultValue());
		timeStopMaxTicks = new ConfigInt(100).initNonNegative();
		timeStopCooldownPerTick = new ConfigFloat(3).initNonNegative();
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
				addTimeStopEffect(user, this, getDuration(user), false);
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
	
	public int getDuration(LivingEntity user) {
		return timeStopMaxTicks.getAsInt();
	}
	
	
	public static class TimeStopWindupAction extends EntityActionInstance {

		public TimeStopWindupAction(EntityActionType ability) {
			super(ability);
			userWalkSpeed = 0;
		}
		
		@Override
		public void actionTick() {
			LivingEntity user = performer;
			if (!user.level().isClientSide()) {
				StandEntity stand = StandUtil.getSummonedStand(user);
				if (stand != null) {
					EntityActionType standWindup = ModSpecialActions.STAND_TIME_STOP_WINDUP.get();
					EntityActionInstance curStandAction = stand.getCurStandAction();
					if (curStandAction == null || curStandAction.ability != standWindup) {
						TimeStopStandWindupAction standWindupAction = new TimeStopStandWindupAction(standWindup);
						standWindupAction.copyFrom(this);
						stand.getStandActionComponent().setAction(standWindupAction, user, SyncType.TRACKING_AND_SELF);
					}
				}
			}
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() == ActionPhase.BUTTON_CHARGE) {
				LivingEntity user = performer;
				if (!user.level().isClientSide()) {
					forceStop();
					syncPhaseChanges();
					
					StandEntity stand = StandUtil.getSummonedStand(user);
					if (stand != null) {
						EntityActionInstance curStandAction = stand.getCurStandAction();
						if (curStandAction != null && curStandAction.ability == ModSpecialActions.STAND_TIME_STOP_WINDUP.get()) {
							curStandAction.forceStop();
							curStandAction.syncPhaseChanges();
						}
					}
				}
			}
		}
		
		@Override
		public void actionPerformStart() {
			LivingEntity user = performer;
			TimeStopAbility ability = (TimeStopAbility) this.ability;
			addTimeStopEffect(user, ability);
		}
		
	}
	
	/* Action that's forced on the Stand entity - 
	 * the real action in on the user (to allow for using the ability without summoning the Stand),
	 * this lets the Stand also have an animation, and disables other Stand attacks during the windup.
	 */
	public static class TimeStopStandWindupAction extends EntityActionInstance {

		public TimeStopStandWindupAction(EntityActionType ability) {
			super(ability);
		}

		public void copyFrom(EntityActionInstance userWindup) {
			this.phasesLength.putAll(userWindup.phasesLength);
			this.skippedWindupPhase = userWindup.skippedWindupPhase;
			this.phase = userWindup.phase;
			this.curPhaseTick = userWindup.curPhaseTick;
			this.curPhaseLength = userWindup.curPhaseLength;
			this.phasePartialTick = userWindup.phasePartialTick;
			phasesLength.put(ActionPhase.RECOVERY, 24); // for the anim
		}
		
	}
	
	
	public static void addTimeStopEffect(LivingEntity user, Consumer<TimeStopEffect> init) {
		Level level = user.level();
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				// should be empty, but just in case
				List<TimeStopEffect> oldEffects = standPower.userStandEffects.getEffectsOfType(ModStandAbilities.EFFECT_TIME_STOP.get()).toList();
				TimeStopEffect timeStop = ModStandAbilities.EFFECT_TIME_STOP.get().create(level);
				init.accept(timeStop);
				standPower.userStandEffects.addEffect(timeStop);
				oldEffects.forEach(effect -> {
					effect.remove();
				});
			}
		}
	}
	
	public static void addTimeStopEffect(LivingEntity user, @Nonnull TimeStopAbility ability) {
		addTimeStopEffect(user, timeStop -> {
			timeStop.timeStopAbility = ability.getAbilityId();
			timeStop.duration = ability.getDuration(user);
			timeStop.cooldownMultiplier = ability.timeStopCooldownPerTick.getAsFloat();
		});
	}
	
	@Deprecated
	public static void addTimeStopEffect(LivingEntity user, @Nullable Ability ability, int duration, boolean stopWhenOtherTSEnds) {
		addTimeStopEffect(user, timeStop -> {
			if (ability != null) {
				timeStop.timeStopAbility = ability.getAbilityId();
			}
			timeStop.duration = duration;
		});
	}


	protected String withDurationTlKey;
	
	@Override
	protected void initVariationAssets() {
		this.withDurationTlKey = tlKey(abilityId, ".duration");
	}

	@Override
	public Component getNameWithContext(Power<?> context) {
		int durationTicks = getDuration(context.getUser());
		return StandSkinComponent.translatable(context, withDurationTlKey, 
				String.format("%.2f", (float) durationTicks / 20F));
	}

}
