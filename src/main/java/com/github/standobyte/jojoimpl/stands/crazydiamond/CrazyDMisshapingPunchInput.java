package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility.StandEntityHeavyPunch;

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
		StandPower standPower = PowerClass.STAND.cast(context);		if (standPower == null) return null;
		StandEntity stand = standPower.getSummonedStandEntity();	if (stand == null) return null;
		EntityActionInstance curAction = stand.getCurStandAction();	if (curAction == null) return null;

		if (curAction instanceof StandEntityHeavyPunch punch 
				&& punch.finisherValue >= 1
				&& punch.getPunchModifiers().isEmpty()) {
			abilities.replaceOtherAbilityWith(standPower, "heavy_punch", this);
		}
		
		return null;
	}
	
	@Override
	public void onClick(Level level, LivingEntity user, FriendlyByteBuf extraClientInput) {
		if (!level.isClientSide()) {
			StandPower standPower = StandPower.get(user);				if (standPower == null) return;
			StandEntity stand = standPower.getSummonedStandEntity();	if (stand == null) return;
			EntityActionInstance curAction = stand.getCurStandAction();	if (curAction == null) return;

			if (curAction instanceof StandEntityHeavyPunch punch
					&& punch.finisherValue >= 1
					&& punch.getPunchModifiers().isEmpty()) {
				StandEffectInstance punchEffect = ModStandAbilities.EFFECT_CD_PUNCH_MISSHAPING.get().create(level);
				standPower.userStandEffects.addEffect(punchEffect);
			}
		}
	}

}
