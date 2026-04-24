package com.github.standobyte.jojoimpl.stands.theworld;

import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.subsystems.timestop.TimeStopInstance;
import com.github.standobyte.jojo.subsystems.timestop.TimeStopLevelTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class TimeStopAbility extends StandEntityAbility {
	public static final int DEFAULT_BUTTON_CHARGE = 40;
	public static final int DEFAULT_WINDUP = 1;
	public static final int DEFAULT_PERFORM = 100;
	public static final int DEFAULT_RECOVERY = 10;

	public TimeStopAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, TimeStopAction::new);

		setDefaultPhaseLength(ActionPhase.BUTTON_CHARGE, DEFAULT_BUTTON_CHARGE);
		setDefaultPhaseLength(ActionPhase.WINDUP, DEFAULT_WINDUP);
		setDefaultPhaseLength(ActionPhase.PERFORM, DEFAULT_PERFORM);
		setDefaultPhaseLength(ActionPhase.RECOVERY, DEFAULT_RECOVERY);
	}

	public static class TimeStopAction extends EntityActionInstance {
		private int activeTimeStopId = -1;

		public static final int START_STAMINA_COST = 1;
		public static final int DEFAULT_RANGE_CHUNKS = 10;
		public static final int TICK_STAMINA_COST = 1;

		public TimeStopAction(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void actionPerformStart() {
			if (level().isClientSide()) {
				return;
			}

			if (!(performer instanceof StandEntity stand)) {
				return;
			}

			LivingEntity user = getPowerUser();
			if (user == null) {
				return;
			}

			StandPower standPower = StandPower.get(user);
			if (standPower == null) {
				return;
			}

			if (!standPower.consumeStamina(START_STAMINA_COST)) {
				startRecovery();
				return;
			}

			ServerLevel serverLevel = (ServerLevel) level();
			TimeStopLevelTracker tracker = TimeStopLevelTracker.get(serverLevel);
			TimeStopInstance instance = tracker.startTimeStop(
					user,
					stand.chunkPosition(),
					DEFAULT_RANGE_CHUNKS,
					100
			);

			this.activeTimeStopId = instance.getId();
		}

		@Override
		public void actionTick() {
			if (level().isClientSide()) {
				return;
			}

			if (getPhase() != ActionPhase.PERFORM) {
				return;
			}

            // EntityActionInstance calls actionTick() before actionPerformStart() on the
            // first PERFORM tick, so the time stop id is not available yet.
            if (activeTimeStopId == -1) {
                return;
            }

			LivingEntity user = getPowerUser();
			if (user == null) {
				startRecovery();
				return;
			}

			StandPower standPower = StandPower.get(user);
			if (standPower == null) {
				startRecovery();
				return;
			}

			ServerLevel serverLevel = (ServerLevel) level();
			TimeStopLevelTracker tracker = TimeStopLevelTracker.get(serverLevel);

			if (!tracker.hasActiveInstance(activeTimeStopId)) {
				startRecovery();
				return;
			}

			if (!standPower.consumeStamina(TICK_STAMINA_COST, true)) {
				tracker.stopTimeStop(activeTimeStopId);
				activeTimeStopId = -1;
				startRecovery();
			}
		}

		@Override
		public void actionPerformEnd() {
			clearTimeStop();
		}

		@Override
		public void onActionCleared(EntityActionInstance newAction) {
			clearTimeStop();
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() == ActionPhase.BUTTON_CHARGE || getPhase() == ActionPhase.PERFORM) {
				clearTimeStop();
				startRecovery();
			}
		}

		private void clearTimeStop() {
			if (level().isClientSide()) {
				return;
			}

			if (activeTimeStopId == -1) {
				return;
			}

			TimeStopLevelTracker tracker = TimeStopLevelTracker.get((ServerLevel) level());
			tracker.stopTimeStop(activeTimeStopId);
			activeTimeStopId = -1;
		}
	}

}
