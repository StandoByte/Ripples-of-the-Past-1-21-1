package com.github.standobyte.jojo.powersystem.ability.input;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputEntry;
import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

public class ActionInputBuffer {
	protected BufferedInputEntry buffered;

	public void bufferClickInput(AbilityId abilityToBuffer) {
		this.buffered = new BufferedInputEntry(abilityToBuffer, InputMethod.CLICK);
	}

	public HeldInput bufferHeldInput(AbilityId abilityToBuffer) {
		this.buffered = new BufferedInputEntry(abilityToBuffer, InputMethod.HOLD);
		return buffered;
	}
	
	public static class BufferingState {
		protected boolean canBuffer;
		protected boolean isAlreadyBuffered;
		public boolean shouldBuffer;
		public boolean isActionSuccess;
		
		public static BufferingState clickCanBuffer() {
			BufferingState obj = new BufferingState();
			obj.canBuffer = true;
			return obj;
		}
		
		protected static final BufferingState CLICK_ONLY = new BufferingState();
		public static BufferingState clickOnly() {
			return CLICK_ONLY;
		}
		
		public static BufferingState buffered() {
			BufferingState obj = new BufferingState();
			obj.isAlreadyBuffered = true;
			return obj;
		}
		
		public boolean canBuffer() {
			return canBuffer;
		}
		
		public boolean isBuffered() {
			return isAlreadyBuffered;
		}
		
		public void setToBuffer() {
			this.shouldBuffer = true;
		}
		
		public void setActionSuccess() {
			this.isActionSuccess = true;
		}
	}

	private FriendlyByteBuf inputBuf = new FriendlyByteBuf(Unpooled.buffer());
	public void tickInputBuffer(EntityActionInputState userInput) {
		LivingEntity user = userInput.user;
		if (user.level().isClientSide()) return;

		if (buffered != null) {
			AbilityId baseAbilityId = buffered.baseAbilityId;
			Power<?> power = baseAbilityId.powerClass().get(user);
			if (power != null && power.hasPower() && baseAbilityId.powerTypeId().equals(power.getPowerType().getId())) {
				AvailableAbilities abilities = power.updateAvailableMoves();
				Ability ability = abilities.inMovesetAndCanBeUsed.get(baseAbilityId.nameInMoveset());
				if (ability != null) {
					BufferingState bufferingState = BufferingState.buffered();
					ability.writeExtraInput(inputBuf, user, false);
					HeldInput newAction = ability.onKeyPress(user.level(), user, inputBuf, 
							buffered.inputMethod, 0, bufferingState);
					inputBuf.clear();
					if (bufferingState.isActionSuccess) {
						for (HeldInputEntry heldKeyAction : userInput.heldKeys.values()) {
							if (heldKeyAction.action == buffered) {
								// Update the held key callback, to be able to stop the new action when the key is released by the player
								heldKeyAction.action = newAction;
								break;
							}
						}
						buffered = null;
					}
				}
			}
			else {
				buffered = null;
			}
		}
	}
	
	public static ActionInputBuffer get(LivingEntity user) {
		EntityActionInputState inputState = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		return inputState != null ? inputState.inputBuffer : null;
	}


	public static record BufferedInputEntry(AbilityId baseAbilityId, InputMethod inputMethod) implements HeldInput {

		@Override
		public void onKeyRelease(LivingEntity user) {
			ActionInputBuffer inputBuffer = ActionInputBuffer.get(user);
			if (inputBuffer == null) return;
			
			// Remove itself from the input buffer, if the key was released before the queued action could start

			if (inputMethod == InputMethod.HOLD) {
				EntityActionInputState inputState = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
				if (inputState != null) {
					if (inputState.inputBuffer.buffered == this) {
						inputState.inputBuffer.buffered = null;
					}
				}
			}
		}

	}
}
