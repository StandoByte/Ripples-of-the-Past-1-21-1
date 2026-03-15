package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.function.Function;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.EntityActionAbility;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class StandEntityAbility extends EntityActionAbility {
	public boolean noFinisherBarDecay = false;

	/**
	 * @deprecated You can use the other constructor, so that you don't have to override {@link EntityActionAbility#createActionObj()}
	 */
	@Deprecated
	public StandEntityAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId);
	}
	
	public StandEntityAbility(AbilityType<?> abilityType, AbilityId abilityId, 
			Function<EntityActionType, ? extends EntityActionInstance> createActionObj) {
		super(abilityType, abilityId, createActionObj);
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		if (level.isClientSide()) return null;
		
		StandPower power = PowerClass.STAND.get(user); if (power == null) return null;
		StandEntity standEntity = power.getSummonedStandEntity(); if (standEntity == null) return null;
		return setOrBufferAction(level, user, standEntity, inputMethod, extraClientInput, clickHoldResolveTime, bufferingState);
	}
	
	
	@Override
	protected LivingEntity getPerformer(LivingEntity user) {
		return StandUtil.getSummonedStand(user);
	}

}
