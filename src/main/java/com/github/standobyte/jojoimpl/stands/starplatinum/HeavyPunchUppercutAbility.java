package com.github.standobyte.jojoimpl.stands.starplatinum;

import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility;

import net.minecraft.world.damagesource.DamageSource;

public class HeavyPunchUppercutAbility extends StandEntityHeavyPunchAbility {

	public HeavyPunchUppercutAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
		this.createActionObj = Uppercut::new;
	}
	
	public static class Uppercut extends StandEntityHeavyPunch {

		public Uppercut(EntityActionType ability) {
			super(ability);
		}
		
		@Override
		protected void addKnockback(DamageSource dmgSource) {
			DamageSourceModified knockback = (DamageSourceModified) dmgSource;
			knockback.jojo_ripples$verticalKnockback(1, 0.8f);
		}
		
	}

}
