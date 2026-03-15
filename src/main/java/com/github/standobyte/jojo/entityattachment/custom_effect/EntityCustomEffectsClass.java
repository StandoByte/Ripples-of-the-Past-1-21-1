package com.github.standobyte.jojo.entityattachment.custom_effect;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public enum EntityCustomEffectsClass {
	STAND_EFFECT {
		@Override public UserStandEffects get(Entity entity, boolean createIfAbsent) {
			return getStandEffects(entity);
		}
	},
	
	OTHER {
		@Override public EntityCustomEffectsMap<EntityCustomEffect> get(Entity entity, boolean createIfAbsent) {
			return getCustomEffects(entity, createIfAbsent);
		}
	};
	
	public abstract EntityCustomEffectsMap<?> get(Entity entity, boolean createIfAbsent);
	
	// generics are so fucking annoying
	public static EntityCustomEffectsMap<EntityCustomEffect> getCustomEffects(Entity entity, boolean createIfAbsent) {
		var type = ModDataAttachmentTypes.ENTITY_CUSTOM_EFFECTS;
		return createIfAbsent ? entity.getData(type) : entity.getExistingDataOrNull(type);
	}
	
	public static UserStandEffects getStandEffects(Entity user) {
		if (user instanceof LivingEntity living) {
			StandPower standPower = StandPower.get(living);
			if (standPower != null) {
				return standPower.userStandEffects;
			}
		}
		return null;
	}
}
