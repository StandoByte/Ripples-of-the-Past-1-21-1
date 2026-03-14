package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public enum EntityCustomEffectsClass {
	STAND_EFFECT {
		@Override
		public UserStandEffects get(Entity entity, boolean createIfAbsent) {
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
		public EntityCustomEffectsMap<?> get(Entity entity, boolean createIfAbsent) {
			var type = ModDataAttachmentTypes.ENTITY_CUSTOM_EFFECTS;
			return createIfAbsent ? entity.getData(type) : entity.getExistingDataOrNull(type);
		}
	};
	
	public abstract EntityCustomEffectsMap<?> get(Entity entity, boolean createIfAbsent);
}
