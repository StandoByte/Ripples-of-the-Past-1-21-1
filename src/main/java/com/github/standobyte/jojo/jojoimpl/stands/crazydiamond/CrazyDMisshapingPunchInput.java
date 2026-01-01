package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility.StandEntityHeavyPunch;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class CrazyDMisshapingPunchInput extends Ability {

	public CrazyDMisshapingPunchInput(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		isSubAbility = true;
	}
	
	@Override
	public Ability replaceWithSubAbility(Power<?> context, AvailableAbilities abilities) {
		StandPower standPower = PowerClass.STAND.cast(context);
		StandEntity stand = standPower.getSummonedStandEntity();
		if (stand != null) {
			EntityActionInstance curAction = stand.getCurStandAction();
			if (curAction != null && curAction instanceof StandEntityHeavyPunch punch && punch.finisherValue >= 1
					&& punch.getPunchModifiers().isEmpty()) {
				abilities.replaceOtherAbilityWith(context, "heavy_punch", this);
			}
		}
		
		return super.replaceWithSubAbility(context, abilities);
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);
			if (standPower != null) {
				StandEntity stand = standPower.getSummonedStandEntity();
				if (stand != null) {
					EntityActionInstance curAction = stand.getCurStandAction();
					if (curAction != null && curAction instanceof StandEntityHeavyPunch punch && punch.finisherValue >= 1
							&& punch.getPunchModifiers().isEmpty()) {
						CrazyDMisshapingPunchEffect punchEffect = ModStandAbilities.EFFECT_CD_PUNCH_MISSHAPING.get().create(level);
						standPower.userStandEffects.addEffect(punchEffect);
					}
				}
			}
		}
	}

}
