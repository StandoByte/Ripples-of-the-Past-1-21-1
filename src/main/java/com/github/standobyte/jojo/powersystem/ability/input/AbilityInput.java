package com.github.standobyte.jojo.powersystem.ability.input;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.event.ModEventHooks;
import com.github.standobyte.jojo.core.event.RipplesAbilityKeyPressEvent;
import com.github.standobyte.jojo.core.packet.fromserver.TrAbilityUsePacket;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputEntry;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class AbilityInput {
	
	public static boolean withConditionCheck(Ability ability, LivingEntity user) {
		if (ability == null || user == null) return false;
		
		Power<?> power = ability.abilityId.powerClass().get(user);
		if (power == null) {
			return false;
		}
		
		AvailableAbilities availableAbilities = power.updateAvailableMoves();
		AbilityConditionCheck abilityConditionCheck = availableAbilities.getAbilityResolved(ability);
		return withConditionCheck(abilityConditionCheck, user);
	}
	
	public static boolean withConditionCheck(AbilityConditionCheck abilityConditionCheck, LivingEntity user) {
		if (abilityConditionCheck == null) {
			return false;
		}
		Ability ability = abilityConditionCheck.ability;
		ConditionCheck result = abilityConditionCheck.conditionCheck;
		boolean canUse = result.isPositive();
		if (!canUse) {
			ConditionCheck.sendActionFailedMessage(ability, result, user);
		}
		return canUse;
	}
	
	
	/* 
	 * FIXME ability inputs that are currently controlled purely by the client
	 * 	barrage can refresh its duration via a client-sent packet
	 * 	the client can use abilities from other movesets
	 */
	@Nullable
	public static HeldInputEntry keyPress(short keyId, Ability ability, 
			LivingEntity user, FriendlyByteBuf extraClientInput, 
			InputMethod inputMethod, float clickHoldResolveTime, 
			BufferingState bufferingState, AbilityId baseAbilityForBuffering) {
		if (ability == null || user == null) return null;
		
		Level level = user.level();
		
		RipplesAbilityKeyPressEvent event = ModEventHooks.onAbilityKeyPress(user, ability, inputMethod, clickHoldResolveTime);
		ability = event.ability;
		HeldInput action;
		if (event.isCanceled()) {
			action = event.newHeldInput;
		}
		else {
			action = ability.onKeyPress(level, user, extraClientInput, inputMethod, clickHoldResolveTime, bufferingState);
			if (bufferingState.canBuffer() && bufferingState.shouldBuffer) {
				HeldInput heldInputObj = null;
				ActionInputBuffer actionInputBuffer = ActionInputBuffer.get(user);
				if (actionInputBuffer != null) {
					if (baseAbilityForBuffering == null) baseAbilityForBuffering = ability.abilityId;
					switch (inputMethod) {
						case CLICK -> actionInputBuffer.bufferClickInput(baseAbilityForBuffering);
						case HOLD -> heldInputObj = actionInputBuffer.bufferHeldInput(baseAbilityForBuffering);
					}
				}
				action = heldInputObj;
			}
		}
		if (!level.isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntity(user, 
					TrAbilityUsePacket.keyPress(user.getId(), keyId, ability, inputMethod, clickHoldResolveTime, user));
		}
		
		
		if (action != null) {
			EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
			if (inputHandler != null) {
				HeldInputEntry heldInput = new HeldInputEntry(keyId, action);
				inputHandler.heldKeys.put(keyId, heldInput);
				return heldInput;
			}
		}
		return null;
	}

	public static void keyRelease(short keyId, LivingEntity user) {
		EntityActionInputState inputHandler = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		if (inputHandler != null) {
			HeldInputEntry heldAction = inputHandler.heldKeys.remove(keyId);
			if (heldAction != null) {
				HeldInput action = heldAction.action;
				Level level = user.level();

				if (action != null) {
					action.onKeyRelease(user);
				}
				if (!level.isClientSide()) {
					PacketDistributor.sendToPlayersTrackingEntity(user, 
							TrAbilityUsePacket.releaseHold(user.getId(), keyId));
				}
			}
		}
	}

	@Nullable
	public static HeldInputEntry keyPressMob(Ability ability, LivingEntity user, FriendlyByteBuf extraData, InputMethod inputMethod) {
		short keyId = (short) pseudoKey.incrementAndGet();
		return keyPress(keyId, ability, user, extraData, inputMethod, 0, BufferingState.clickOnly(), null);
	}
	private static final AtomicInteger pseudoKey = new AtomicInteger();
	

	public enum InputEventType {
		PRESS_CLICK(InputMethod.CLICK),
		PRESS_HOLD(InputMethod.HOLD),
		RELEASE(InputMethod.HOLD);
		
		public final InputMethod inputMethod;

		InputEventType(InputMethod inputMethod) {
			this.inputMethod = inputMethod;
		}
	}
	
}
