package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.jojo.core.NotYetImplemented;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public enum EntityAttachmentsClass {
	STAND_EFFECT {
		@Override
		public UserStandEffects get(Entity entity) {
			if (entity instanceof LivingEntity living) {
				StandPower standPower = StandPower.get(living);
				if (standPower != null) {
					return standPower.userStandEffects;
				}
			}
			return null;
		}
	},
	OTHER {
		@Override
		public EntityAttachmentsHolder<?> get(Entity entity) {
			throw new NotYetImplemented();
		}
	};
	
	public abstract EntityAttachmentsHolder<?> get(Entity entity);
}
