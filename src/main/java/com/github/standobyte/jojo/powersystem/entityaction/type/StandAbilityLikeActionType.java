package com.github.standobyte.jojo.powersystem.entityaction.type;

import java.util.function.Function;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class StandAbilityLikeActionType extends SpecialEntityActionType {

	public StandAbilityLikeActionType(ResourceLocation id, 
			Function<EntityActionType, ? extends EntityActionInstance> createActionObj) {
		super(null, id, createActionObj);
		this.anim = ActionAnimIdentifier.getOrCreate(id.getPath(), false);
		this.abilityId = new AbilityId(PowerClass.STAND, JojoMod.resLoc("special"), id.toString());
	}

	@Override
	public ResourceLocation getEntityAnimSet(LivingEntity user) {
		return EntityActionType.getPowerAnimSet(PowerClass.STAND, user);
	}
	
}
