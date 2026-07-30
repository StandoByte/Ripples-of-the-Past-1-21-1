package com.github.standobyte.jojo.mechanics.jojopose;

import java.util.List;

import com.github.standobyte.jojo.client.entityanim.LivingAnimState;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.mechanics.jojopose.resource.ClientJojoPoseLoader;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPose;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPoseAnimSet2;
import com.github.standobyte.jojo.mechanics.voiceline.ClientVoiceLineDefinition;
import com.github.standobyte.jojo.mechanics.voiceline.VoiceLineClientSide;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.subsystems.movement_input_sync.PlayerMovementInputData;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class JojoPoseActionType extends SpecialEntityActionType {

	public JojoPoseActionType(ResourceLocation id) {
		super(null, id, PosingInstance::new);
	}

	public static class PosingInstance extends EntityActionInstance {
		public ResourceLocation animationSet;
		public String animName;
		public ActionAnimIdentifier poseId;

		public PosingInstance(ResourceLocation animationSet, String animName) {
			this(ModSpecialActions.JOJO_POSE.get());
			this.animationSet = animationSet;
			this.animName = animName;
			this.setStartingPhase();
		}

		protected PosingInstance(EntityActionType ability) {
			super(ability);
			phasesLength.put(ActionPhase.PERFORM, Float.MAX_VALUE);
		}
		
		
		@Override
		public void toBuf(FriendlyByteBuf buf) {
			ResourceLocation.STREAM_CODEC.encode(buf, animationSet);
			buf.writeUtf(animName);
		}

		@Override
		public void fromBuf(FriendlyByteBuf buf) {
			animationSet = ResourceLocation.STREAM_CODEC.decode(buf);
			animName = buf.readUtf();
			poseId = ActionAnimIdentifier.getOrCreate(animName, 0);
		}
		
		@Override
		public void extractAnim(LivingAnimState animVariables, LivingEntity performer, float partialTick) {
			super.extractAnim(animVariables, performer, partialTick);
			animVariables.animFromJojoPosesLoader = true;
		}

		@Override
		public ResourceLocation getEntityAnimSet() {
			return animationSet;
		}

		@Override
		public ActionAnimIdentifier getEntityAnim() {
			return poseId;
		}

		
		@Override
		public void actionPerformStart() {
			if (level().isClientSide()) {
				JojoPoseAnimSet2 animSet = ClientJojoPoseLoader.getInstance().getAnimSet(animationSet);
				if (animSet != null) {
					JojoPose jojoPose = animSet.getPose(poseId.getOriginalAnimName());
					if (jojoPose != null) {
						List<ClientVoiceLineDefinition> voiceLines = jojoPose.voiceLine;
						if (voiceLines != null && !voiceLines.isEmpty()) {
							ClientVoiceLineDefinition voiceLine = VoiceLineClientSide.pick(voiceLines);
							VoiceLineClientSide.play(voiceLine, performer, false);
						}
					}
				}
			}
		}
		
		@Override
		public void actionTick() {
			if (!level().isClientSide()) {
				boolean stopPosing = performer.swinging;
				if (!stopPosing) {
					PlayerMovementInputData input = PlayerMovementInputData.get(performer);
					if (input != null) {
						stopPosing |= input.jumping || input.shiftKeyDown || input.left != 0 || input.forward != 0;
					}
				}
				
				if (stopPosing) {
					forceStop();
					syncPhaseChanges();
				}
			}
		}
		
		
		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}
		
	}
	
}
