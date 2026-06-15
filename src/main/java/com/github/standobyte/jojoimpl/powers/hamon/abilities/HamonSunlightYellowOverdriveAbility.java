package com.github.standobyte.jojoimpl.powers.hamon.abilities;

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

public class HamonSunlightYellowOverdriveAbility extends EntityActionAbility {

	public HamonSunlightYellowOverdriveAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, SYOverdrive::new);
		setDefaultPhaseLength(ActionPhase.WINDUP, 40);
		setDefaultPhaseLength(ActionPhase.PERFORM, 10);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 7);
	}

	public static class SYOverdrive extends EntityActionInstance {
		
		public SYOverdrive(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		public void onButtonStopHold() {
			if (getPhase() == ActionPhase.WINDUP) {
				if (getPhaseTick() >= 10) {
					setPhaseStart(ActionPhase.PERFORM);
					syncPhaseChanges();
				}
				else {
					forceStop();
					syncPhaseChanges();
				}
			}
		}

		@Override
		public void actionPerformStart() {
			LivingEntity user = performer;
			HamonData hamon = PlayerPower.getPowerData(user, ModPlayerPowers.HAMON).orElse(null);
			if (hamon != null) {
				hamon.energy.energyAmount.set(0, false);
			}
		}
	}
	
}
