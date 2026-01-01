package com.github.standobyte.jojo.powersystem.ability.input;

import java.util.HashMap;
import java.util.Map;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState.HeldInputEntry;

import io.netty.buffer.Unpooled;

import com.github.standobyte.jojo.powersystem.entityaction.HeldInput;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

public class ActionInputBuffer {
	protected Map<LivingEntity, BufferedInputEntry> bufferPerPerformer = new HashMap<>();

	public void bufferClickInput(LivingEntity performer, AbilityId abilityToBuffer) {
		bufferPerPerformer.put(performer, new BufferedInputEntry(abilityToBuffer, InputMethod.CLICK));
	}

	public HeldInput bufferHeldInput(LivingEntity performer, AbilityId abilityToBuffer) {
		BufferedInputEntry inputBuffer = new BufferedInputEntry(abilityToBuffer, InputMethod.HOLD);
		bufferPerPerformer.put(performer, inputBuffer);
		return inputBuffer;
	}
	
	public static class BufferingState {
		
		protected boolean canBuffer;
		protected boolean isAlreadyBuffered;
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
		
		public void setActionSuccess() {
			this.isActionSuccess = true;
		}
	}

	private FriendlyByteBuf inputBuf = new FriendlyByteBuf(Unpooled.buffer());
	public void tickInputBuffer(EntityActionInputState userInput) {
		LivingEntity user = userInput.user;
		if (user.level().isClientSide()) return;
		
		var entryIter = bufferPerPerformer.entrySet().iterator();
		while (entryIter.hasNext()) {
			var entry = entryIter.next();
			LivingEntity performer = entry.getKey();
			if (performer == null || !performer.isAlive()) {
				entryIter.remove();
			}
			else {
				BufferedInputEntry bufferedInput = entry.getValue();
				if (bufferedInput != null) {
					AbilityId abilityId = bufferedInput.abilityId;
					Power<?> power = abilityId.powerClass().get(user);
					if (power != null && power.hasPower() && abilityId.powerTypeId().equals(power.getPowerType().getId())) {
						AvailableAbilities abilities = power.updateAvailableMoves();
						Ability ability = abilities.inMovesetAndCanBeUsed.get(abilityId.nameInMoveset());
						if (ability != null) {
							BufferingState bufferingState = BufferingState.buffered();
							ability.writeExtraInput(inputBuf, user, false);
							HeldInput newAction = ability.onKeyPress(user.level(), user, inputBuf, 
									bufferedInput.inputMethod, 0, bufferingState);
							inputBuf.clear();
							if (bufferingState.isActionSuccess) {
								for (HeldInputEntry heldKeyAction : userInput.heldKeys.values()) {
									if (heldKeyAction.action == bufferedInput) {
										// Update the held key callback, to be able to stop the new action when the key is released by the player
										heldKeyAction.action = newAction;
										break;
									}
								}
								entryIter.remove();
							}
						}
					}
					else {
						entryIter.remove();
					}
				}
			}
		}
	}
	
	public static ActionInputBuffer get(LivingEntity user) {
		EntityActionInputState inputState = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
		return inputState != null ? inputState.inputBuffer : null;
	}


	public static record BufferedInputEntry(AbilityId abilityId, InputMethod inputMethod) implements HeldInput {

		@Override
		public void onKeyRelease(LivingEntity user) {
			ActionInputBuffer inputBuffer = ActionInputBuffer.get(user);
			if (inputBuffer == null) return;
			
			// Remove itself from the input buffer, if the key was released before the queued action could start

			if (inputMethod == InputMethod.HOLD) {
				EntityActionInputState inputState = user.getData(ModDataAttachmentTypes.ENTITY_ABILITY_INPUT.get());
				if (inputState != null) {
					var entryIter = inputBuffer.bufferPerPerformer.entrySet().iterator();
					while (entryIter.hasNext()) {
						var entry = entryIter.next();
						if (entry.getValue() == this) {
							entryIter.remove();
							break;
						}
					}
				}
			}
		}

	}
}
