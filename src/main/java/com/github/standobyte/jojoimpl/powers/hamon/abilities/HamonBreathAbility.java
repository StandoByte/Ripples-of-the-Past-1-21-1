package com.github.standobyte.jojoimpl.powers.hamon.abilities;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;

import net.minecraft.world.entity.LivingEntity;

public class HamonBreathAbility extends EntityActionAbility {

	public HamonBreathAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, HamonBreath::new);
		setButtonHoldPhase(ActionPhase.PERFORM);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 5); // just for the animation
	}

	public static class HamonBreath extends EntityActionInstance {
		HamonData hamon;
		
		public HamonBreath(EntityActionType ability) {
			super(ability);
			userWalkSpeed = 0;
		}
		
		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			LivingEntity user = performer;
			boolean clientSide = user.level().isClientSide();
			if (!clientSide || user == ClientProxy.getClientPlayer()) {
				hamon = PlayerPower.getPowerData(user, ModPlayerPowers.HAMON).orElse(null);
			}
		}

		@Override
		public void onButtonStopHold() {
			startRecovery();
		}

		@Override
		public void onSetPhase(ActionPhase newPhase) {
			if (hamon != null) {
				if (newPhase == ActionPhase.PERFORM) {
					hamon.energy.startHamonBreath();
				}
				else if (this.getPhase() == ActionPhase.PERFORM && hamon != null) {
					hamon.energy.stopHamonBreath();
				}
			}
		}
		
		@Override
		public void onActionCleared(@Nullable EntityActionInstance newAction) {
			if (hamon != null) {
				hamon.energy.stopHamonBreath();
			}
		}
		
	}
	
}
