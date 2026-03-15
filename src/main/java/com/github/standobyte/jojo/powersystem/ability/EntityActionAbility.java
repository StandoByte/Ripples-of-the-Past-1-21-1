package com.github.standobyte.jojo.powersystem.ability;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ui.hud_power.WindupIndicator;
import com.github.standobyte.jojo.config.MolangValue;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityActionAbility extends Ability implements EntityActionType {
	protected Function<EntityActionType, ? extends EntityActionInstance> createActionObj;
	protected ActionAnimIdentifier anim;

	/**
	 * @deprecated You can use the other constructor, so that you don't have to override {@link EntityActionAbility#createActionObj()}
	 */
	@Deprecated
	public EntityActionAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		this(abilityType, abilityId, EntityActionInstance::new);
	}
	
	public EntityActionAbility(AbilityType<?> abilityType, AbilityId abilityId, 
			Function<EntityActionType, ? extends EntityActionInstance> createActionObj) {
		super(abilityType, abilityId);
		this.createActionObj = createActionObj;
		anim = ActionAnimIdentifier.getOrCreate(abilityId);
	}
	
	@Override
	public EntityActionInstance createActionObj() {
		return createActionObj.apply(this);
	}
	
	
	@Override
	public HeldInput onKeyPress(Level level, LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, BufferingState bufferingState) {
		if (level.isClientSide()) return null;
		return setOrBufferAction(level, user, user, inputMethod, extraClientInput, clickHoldResolveTime, bufferingState);
	}
	
	@Nullable
	public HeldInput setOrBufferAction(Level level, LivingEntity user, LivingEntity performer, 
			InputMethod inputMethod, FriendlyByteBuf extraClientInput, 
			float skipWindupTime, BufferingState bufferingState) {
		if (user == null || inputMethod == null) return null;
		EntityActionInstance action = initActionOnAbilityUse(level, user, performer, extraClientInput);
		if (action == null) return null;
		
		LivingComponentAction actionComponent = LivingComponentAction.getComponent(performer);
		boolean toBuffer = action.ability.shouldBufferInput(actionComponent);
		
		HeldInput actionOrQueue = null;
		if (toBuffer) {
			if (bufferingState.canBuffer()) {
				bufferingState.setToBuffer();
			}
		}
		else {
			if (skipWindupTime > 0) {
				action.skipWindupTime(performer, skipWindupTime);
			}
			actionOrQueue = actionComponent.setAction(action, user, SyncType.TRACKING_AND_SELF);
			bufferingState.setActionSuccess();
		}
		return actionOrQueue;
	}
	
	
	/**
	 * Is used to initialize some EntityActionInstance values, 
	 * that are either configurable (stuff like phases length), 
	 * or are context-dependent.
	 */
	@ApiStatus.OverrideOnly
	public void initActionFromConfig(EntityActionInstance action, Level level, 
			LivingEntity powerUser, LivingEntity performer) {
		var map = action.phasesLength;
		if (!map.containsKey(ActionPhase.BUTTON_CHARGE))	map.put(ActionPhase.BUTTON_CHARGE,	buttonChargePhase.getAsFloat());
		if (!map.containsKey(ActionPhase.WINDUP))			map.put(ActionPhase.WINDUP,			windupPhase.getAsFloat());
		if (!map.containsKey(ActionPhase.PERFORM))			map.put(ActionPhase.PERFORM,		performPhase.getAsFloat());
		if (!map.containsKey(ActionPhase.RECOVERY))			map.put(ActionPhase.RECOVERY,		recoveryPhase.getAsFloat());
	}

	protected MolangValue buttonChargePhase = new MolangValue.Literal(0);
	protected MolangValue windupPhase = new MolangValue.Literal(0);
	protected MolangValue performPhase = new MolangValue.Literal(1);
	protected MolangValue recoveryPhase = new MolangValue.Literal(0);
	
	public void setDefaultPhaseLength(ActionPhase phase, float length) {
		switch (phase) {
			case BUTTON_CHARGE -> buttonChargePhase = new MolangValue.Literal(length);
			case WINDUP -> windupPhase = new MolangValue.Literal(length);
			case PERFORM -> performPhase = new MolangValue.Literal(length);
			case RECOVERY -> recoveryPhase = new MolangValue.Literal(length);
		}
	}
	
	@Nullable protected ActionPhase buttonHoldingPhase;
	public void setButtonHoldPhase(@Nonnull ActionPhase buttonHoldingPhase) {
		this.buttonHoldingPhase = buttonHoldingPhase;
		setDefaultPhaseLength(buttonHoldingPhase, 999999);
	}
	

	@Override
	public ActionAnimIdentifier getEntityAnim(EntityActionInstance action) {
		return anim;
	}
	
	
	protected LivingEntity getPerformer(LivingEntity user) {
		return user;
	}
	
	@Override
	public WindupIndicator cl_windupIndicator(LivingEntity clientPlayer, WindupIndicator indicator, float partialTick) {
		indicator.maxValue = buttonChargePhase.getAsFloat() > 0 ? 1 : 0;
		indicator.value = -1;
		
		if (indicator.maxValue > 0) {
			LivingEntity performer = getPerformer(clientPlayer);
			if (performer != null) {
				EntityActionInstance curAction = LivingComponentAction.getCurEntityAction(performer);
				if (curAction != null && curAction.ability == this) {
					if (curAction.getPhase() == ActionPhase.BUTTON_CHARGE) {
						indicator.maxValue = curAction.getAnimPhaseLength();
						if (indicator.maxValue > 0) {
							indicator.value = curAction.getAnimPhaseTick(partialTick);
						}
					}
					else {
						indicator.maxValue = curAction.phasesLength.getFloat(ActionPhase.BUTTON_CHARGE);
						indicator.value = indicator.maxValue;
					}
				}
			}
			return indicator;
		}
		
		return super.cl_windupIndicator(clientPlayer, indicator, partialTick);
	}

}
