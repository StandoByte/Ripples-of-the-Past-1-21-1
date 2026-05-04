package com.github.standobyte.jojo.powersystem.entityaction.type;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.AbilityId.AbilityInputNetwork;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface EntityActionType {
	/**
	 * Is used for synchronization as well, so it only needs to create the object itself.
	 */
	@ApiStatus.OverrideOnly
	default EntityActionInstance createActionObj() {
		return new EntityActionInstance(this);
	}
	
	AbilityId getAbilityId();
	
	AbilityUsageGroup getAbilityUsageCategory();


	/**
	 * A helper method to create the action to be set to the performer when they user uses an ability (e.g. a Hamon attack).
	 * @param level The method is called both on server and on the player user's client.
	 * @param powerUser The player/mob the ability belongs to.
	 * @param performer The entity actually performing the action, can be the user entity itself or the user's Stand entity.
	 * @param extraInput The extra input needed for some abilities to work, defined by overriding {@link Ability#writeExtraInput(FriendlyByteBuf, LivingEntity)}
	 * @return The instance of the action, to be able to stop it when the player stops holding the button.
	 */
	default EntityActionInstance initActionOnAbilityUse(Level level, LivingEntity powerUser, LivingEntity performer, @Nullable FriendlyByteBuf extraInput) {
		EntityActionInstance action = createActionObj();
		initActionFromConfig(action, level, powerUser, performer);
		action.setStartingPhase();
		if (extraInput != null) {
			action.extraClientInput(extraInput);
		}
		return action;
	}

	@ApiStatus.OverrideOnly
	default void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		for (ActionPhase phase : ActionPhase.values()) {
			action.phasesLength.put(phase, phase == ActionPhase.PERFORM ? 1f : 0f);
		}
	}


	@ApiStatus.Internal
	default void encodeAbility(LivingEntity user, FriendlyByteBuf buffer) {
		buffer.writeBoolean(true);
		AbilityInputNetwork.encodeInput(buffer, user, (Ability) this);
	}

	@ApiStatus.Internal
	public static EntityActionInstance decodeAbilityAction(Level level, FriendlyByteBuf buffer) {
		boolean isPowerMovesetAbility = buffer.readBoolean();
		EntityActionType actionType;
		if (isPowerMovesetAbility) {
			Ability ability = AbilityInputNetwork.decodeInput(buffer).getAbility(null, level);
			actionType = ability instanceof EntityActionType entityAbility ? entityAbility : null;
		}
		else {
			ResourceLocation specialActionId = buffer.readResourceLocation();
			actionType = JojoRegistries.NON_POWER_ACTIONS_REG.get(specialActionId);
		}
		return actionType != null ? actionType.createActionObj() : null;
	}


	@ApiStatus.OverrideOnly
	default boolean shouldBufferInput(LivingComponentAction performerAction) {
		EntityActionInstance curAction = performerAction.getAction();
		return curAction != null && !curAction.canBeCancelledInto(this);
	}
	
	
	default ResourceLocation getEntityAnimSet(LivingEntity user) {
		AbilityId abilityId = getAbilityId();
		PowerClass<?> powerClass = abilityId != null ? abilityId.powerClass() : null;
		if (powerClass == null) {
			powerClass = PowerClass.PLAYER_POWER;
		}
		return getPowerAnimSet(powerClass, user);
	}
	
	static ResourceLocation getPowerAnimSet(PowerClass<?> powerClass, LivingEntity user) {
		Power<?> power = powerClass.get(user);
		if (power != null && power.hasPower()) {
			return power.getPowerType().getId();
		}
		return null;
	}

	ActionAnimIdentifier getEntityAnim(EntityActionInstance action);
}
