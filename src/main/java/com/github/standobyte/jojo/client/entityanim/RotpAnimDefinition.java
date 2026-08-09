package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.action.AnimInstructionTimelines;
import com.github.standobyte.jojo.client.entityanim.action.AnimObjTimeline;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.AnimationChannelQuery;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.IAnimationChannel;
import com.github.standobyte.jojo.client.entityanim.molang.animelement.KeyframeQuery;
import com.github.standobyte.jojo.client.entityanim.player.PlayerAnimRigModel;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityrender.HiddenModelPartsUtil;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.jojo.util.objects_java.OptionalFloat;
import com.github.standobyte.v1_21_4_stuff.OldPlayerModelJank;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class RotpAnimDefinition {
	public final float lengthInSeconds;
	public final OptionalFloat loopBackTo;
	protected final Map<String, List<IAnimationChannel>> boneAnimations;
	protected final List<KeyframeQuery> queries;
	public final AnimInstructionTimelines instructionTimelines;
	@Nullable public Map<String, SavedPose> poses;
	@Nullable public AnimationMirror animationMirror;
	
	public RotpAnimDefinition(float lengthInSeconds, OptionalFloat loopBackTo, 
			Map<String, List<IAnimationChannel>> boneAnimations, 
			AnimInstructionTimelines instructionTimelines, 
			@Nullable Map<String, SavedPose> poses) {
		this.lengthInSeconds = lengthInSeconds;
		this.loopBackTo = loopBackTo;
		
		this.boneAnimations = boneAnimations;
		this.queries = boneAnimations.entrySet().stream().flatMap(entry -> entry.getValue().stream())
				.map(channel -> (AnimationChannelQuery) channel)
				.flatMap(channel -> Arrays.stream(channel.rotpKeyframes()))
				.filter(keyframe -> !keyframe.isNumericLiteral())
				.toList();
		
		this.instructionTimelines = instructionTimelines;
		this.poses = poses;
	}
	
	public RotpAnimDefinition copyWithAnim(Map<String, List<IAnimationChannel>> boneAnimations) {
		RotpAnimDefinition copy = new RotpAnimDefinition(lengthInSeconds, loopBackTo, 
				boneAnimations, 
				instructionTimelines, 
				null);
		return copy;
	}
	
	public void postInit(@Nullable Object2FloatMap<_PoseNameInit> poseTimestamps) {
		if (poseTimestamps != null) {
			poses = new HashMap<>(poseTimestamps.size());
			for (var timestampEntry : poseTimestamps.object2FloatEntrySet()) {
				_PoseNameInit key = timestampEntry.getKey();
				String poseName = key.name;
				boolean addToStandInfoScreen = key.isCoolPose;
				float timestamp = timestampEntry.getFloatValue();
				AnimFramePose frame = new AnimFramePose();
				calcAnimPose(frame, timestamp, 1, null, null);
				poses.put(poseName, new SavedPose(frame, timestamp, addToStandInfoScreen));
			}
		}
	}
	
	protected static record _PoseNameInit(String name, boolean isCoolPose) {}
	public static record SavedPose(AnimFramePose pose, float timeInSeconds, boolean addToStandInfoScreen) {}


	public AnimFramePose calcAnimPose(float seconds, float animSpeed,
			@Nullable AnimMolangVariables animVariables, 
			@Nullable AnimFramePose prevPunchPose) {
		AnimFramePose pose = AnimFramePose.reused.clear();
		calcAnimPose(pose, seconds, animSpeed, 
				animVariables, 
				prevPunchPose);
		return pose;
	}

	public void calcAnimPose(AnimFramePose dest, float seconds, float animSpeed, 
			@Nullable AnimMolangVariables animVariables, 
			@Nullable AnimFramePose prevPunchPose) {
		evaluateQueries(animVariables);

		Map<String, List<IAnimationChannel>> anim = SmoothPunchComboAnimTransition.transition(boneAnimations, prevPunchPose);
		for (Map.Entry<String, List<IAnimationChannel>> entry : anim.entrySet()) {
			ModelPartFrame modelPartPose = dest.getForModelPart(entry.getKey());
			for (IAnimationChannel tf : entry.getValue()) {
				Vector3f vec = calcVec(this, tf, seconds, animSpeed);
				modelPartPose.set(vec, tf.target());
			}
		}
	}
	
	public static void animateVanillaHumanoid(PlayerAnimRigModel rigModel, HumanoidModel<?> vanillaModel, AnimFramePose pose) {
		/* This vanilla part is not referenced in playerAnimator format, so we just reset it, 
		 * in order to get rid of things like y rotation from the vanilla punch animation.
		 */
		vanillaModel.body.resetPose();
		EntityRenderState.resetPose(rigModel);
		animate(rigModel, pose);
		OldPlayerModelJank._onAnimate(vanillaModel);
	}
	
	public static void animate(Model model, AnimFramePose pose) {
		Model_1_21_2plus backportModelCast = (Model_1_21_2plus) model;
		ModelWithExtraFeatures rotpModelCast = (ModelWithExtraFeatures) model;
		
		for (var modelPartEntry : pose.pose.entrySet()) {
			String modelPartName = modelPartEntry.getKey();
			ModelPart modelPart = getModelPart(modelPartName, model, backportModelCast);
			if (modelPart != null) {
				HiddenModelPartsUtil.onAnimate(rotpModelCast, modelPart);
				modelPartEntry.getValue().apply(modelPart);
			}
		}
	}

	@Deprecated
	public AnimFramePose calcAnimPose(@Nullable AnimMolangVariables animVariables, 
			@Nullable AnimFramePose prevPunchPose, float seconds, float animSpeed) {
		return calcAnimPose(seconds, animSpeed, animVariables, prevPunchPose);
	}
	
	@Deprecated
	public AnimFramePose animate(Model model, LivingEntity entity, 
			LivingComponentAction actionComponent, float seconds, float animSpeed, float partialTick) {
		AnimFramePose frame = calcAnimPose(seconds, animSpeed, 
				AnimMolangVariables.extract(entity, partialTick), 
				actionComponent != null ? actionComponent.clPrevPunchPose : null);
		animate(model, frame);
		return frame;
	}
	
	@Deprecated
	public AnimFramePose animate(Model model, @Nullable AnimMolangVariables animVariables, 
			@Nullable AnimFramePose prevPunchPose, float seconds, float animSpeed) {
		AnimFramePose frame = calcAnimPose(seconds, animSpeed, animVariables, prevPunchPose);
		animate(model, frame);
		return frame;
	}
	

	public static ModelPart getModelPart(String animBoneName, Model model, Model_1_21_2plus rotpModelCast) {
		if (rotpModelCast != null) {
			Optional<ModelPart> modelPart = rotpModelCast.jojo_ripples$getAnyDescendantWithName(animBoneName);
			if (modelPart.isPresent()) return modelPart.get();
		}
		return null;
	}
	
	
	/**
	 * @return action anim time in seconds
	 */
	public float getAnimTime(LivingAnimState entityAction) {
		float animSeconds = 0;

		boolean appliedPhaseAnim = false;
		boolean usePhaseTime = entityAction.actionPhase != null && this.instructionTimelines.phases != null;
		if (usePhaseTime) {
			ActionPhase taskPhase = entityAction.actionPhase;

			Float2ObjectMap.Entry<AnimActionPhase> curPhase = null;
			Float2ObjectMap.Entry<AnimActionPhase> nextPhase = null;

			@Nonnull Float2ObjectMap.Entry<AnimActionPhase> iterPrevPhase = null;
			for (Float2ObjectMap.Entry<AnimActionPhase> animPhase : this.instructionTimelines.phases.getEntries()) {
				if (taskPhase.ordinal() < animPhase.getValue().phase.ordinal()) {
					curPhase = iterPrevPhase;
					nextPhase = animPhase;
					break;
				}
				iterPrevPhase = animPhase;
			}
			if (iterPrevPhase == null) {
				usePhaseTime = false;
			}
			else {
				ActionPhase lastAnimPhase = iterPrevPhase.getValue().phase;
				if (curPhase == null) {
					if (taskPhase == lastAnimPhase) {
						curPhase = iterPrevPhase;
					}
				}
				if (curPhase != null) {
					float curPhaseTime = curPhase.getFloatKey();
					float nextPhaseTime = nextPhase != null ? nextPhase.getFloatKey() : this.lengthInSeconds;
					switch (curPhase.getValue().timeAnimMode) {
						case FIT_PHASE_LENGTH -> {
							animSeconds = Mth.lerp(entityAction.phaseCompletion, curPhaseTime, nextPhaseTime);
							appliedPhaseAnim = true;
						}
						case CONSTANT_LENGTH -> {
							float phaseSecs = entityAction.phaseTime / 20f;
							animSeconds = curPhaseTime + phaseSecs;
//							if (entity != null && animSeconds >= this.animation.lengthInSeconds()) {
//								entity.onSetPoseAnimEnded();
//							}
							appliedPhaseAnim = true;
						}
						case LOOP_BACK -> {
							float loopLen = nextPhaseTime - curPhase.getValue().loopBackTo;
							float phaseSecs = entityAction.phaseTime / 20f;
							if (phaseSecs < nextPhaseTime - curPhaseTime) { // loop hasn't started yet
								animSeconds = curPhaseTime + phaseSecs;
							}
							else {
								animSeconds = curPhase.getValue().loopBackTo + (phaseSecs - (curPhase.getValue().loopBackTo - curPhaseTime)) % loopLen;
							}
							appliedPhaseAnim = true;
						}
					}
				}
				else if (taskPhase.ordinal() > lastAnimPhase.ordinal()) {
					animSeconds = this.lengthInSeconds;
					appliedPhaseAnim = true;
				}
			}
		}

		if (!appliedPhaseAnim) {
			float time = usePhaseTime ? entityAction.phaseTime : entityAction.time;
			animSeconds = getAnimTime(time);
		}
		return animSeconds;
	}
	
	/**
	 * @return anim time in seconds
	 */
	public float getAnimTime(float ticks) {
		float time = ticks / 20f;
		if (loopBackTo.isPresent()) {
			float loopBackTo = this.loopBackTo.getAsFloat();
			if (ticks > lengthInSeconds) {
				float loopLen = lengthInSeconds - loopBackTo;
				time = (time - loopBackTo) % loopLen + loopBackTo;
			}
		}
		return time;
	}
	

	protected static final Vector3f TARGET = new Vector3f();
	
	public static Vector3f calcVec(RotpAnimDefinition anim, IAnimationChannel tf, float seconds, float animSpeed) {
		Keyframe[] keyframes = tf.keyframes();
		Vector3f vec = anim.lerpKeyframes(keyframes, seconds, animSpeed);
		adjustBlockbenchVec(tf.target(), vec);
		return vec;
	}
	
	// can't move this to parsing because of Molang
	public static void adjustBlockbenchVec(AnimationChannel.Target target, Vector3f vec) {
		if (target == AnimationChannel.Targets.ROTATION) {
			vec.mul(MathUtil.DEG_TO_RAD);
		}
		else if (target == AnimationChannel.Targets.POSITION) {
			vec.mul(1, -1, 1);
		}
		else if (target == AnimationChannel.Targets.SCALE) {
			vec.add(-1, -1, -1);
		}
	}
	
	public Vector3f lerpKeyframes(Keyframe[] keyframes, float seconds, float animSpeed) {
		int i = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
		int j = Math.min(keyframes.length - 1, i + 1);
		Keyframe keyframe = keyframes[i];
		Keyframe keyframe2 = keyframes[j];
		float h = seconds - keyframe.timestamp();
		float k = j != i ? Mth.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
		keyframe2.interpolation().apply(TARGET, k, keyframes, i, j, animSpeed);
		return TARGET;
	}
	
	
	private void evaluateQueries(@Nullable AnimMolangVariables animVariables) {
		if (animVariables != null) 	AnimMolangQuery.instance.fillContext(animVariables);
		else						AnimMolangQuery.instance.reset();
		queries.forEach(KeyframeQuery::evaluate);
	}
	

	public static class TimelineKeys {
		public static final String BARRAGE = "barrage";
	}
	
	
	public static class Builder {
		protected float length;
		protected final Map<String, List<IAnimationChannel>> animationByBone = Maps.newHashMap();
		protected OptionalFloat loopBackTo = OptionalFloat.empty();
		protected final AnimInstructionTimelines instructions = new AnimInstructionTimelines();
		protected Object2FloatMap<_PoseNameInit> poses;
		
		@Nullable public HumanoidArm mirrorDefaultSide = null;
		public float mirrorStart = 0;
		public float mirrorEnd;
		
		public Builder(float lengthInSeconds) {
			this.length = lengthInSeconds;
			this.mirrorEnd = lengthInSeconds;
		}

		public RotpAnimDefinition.Builder looping() {
			return looping(0);
		}

		public RotpAnimDefinition.Builder looping(float loopBackToSec) {
			this.loopBackTo = OptionalFloat.of(loopBackToSec);
			return this;
		}

		public RotpAnimDefinition.Builder addAnimation(String bone, IAnimationChannel animationChannel) {
			this.animationByBone.computeIfAbsent(bone, p_329694_ -> new ArrayList<>()).add(animationChannel);
			return this;
		}
		
		public RotpAnimDefinition.Builder addActionPhaseKeyframe(AnimActionPhase value, float time) {
			if (instructions.phases == null) {
				instructions.phases = new AnimObjTimeline<>();
			}
			instructions.phases.add(time, value);
			return this;
		}
		
		public RotpAnimDefinition.Builder addFieldValueKeyframe(String field, String value, float time) {
			if (instructions.stringVals == null) {
				instructions.stringVals = new HashMap<>();
			}
			AnimObjTimeline<String> timeline = instructions.stringVals.computeIfAbsent(field, __ -> new AnimObjTimeline<>());
			timeline.add(time, value);
			return this;
		}
		
		public RotpAnimDefinition.Builder addPoseTimestamp(String name, float time, boolean coolPose) {
			if (poses == null) {
				poses = new Object2FloatArrayMap<>();
			}
			poses.put(new _PoseNameInit(name, coolPose), time);
			return this;
		}
		
		public RotpAnimDefinition build() {
			instructions.onFinishedParsing();
			RotpAnimDefinition anim = new RotpAnimDefinition(length, loopBackTo, animationByBone, instructions, null);
			
			anim.postInit(this.poses);
			
			if (mirrorDefaultSide != null) {
				anim.animationMirror = new AnimationMirror(mirrorDefaultSide, mirrorStart, mirrorEnd);
			}
			
			return anim;
		}
	}
	
	
	public static class AnimWithId {
		public static AnimWithId instance = new AnimWithId();
		
		public ActionAnimIdentifier animId;
		public RotpAnimDefinition anim;
		
		public static AnimWithId with(ActionAnimIdentifier animId, RotpAnimDefinition anim) {
			instance.animId = animId;
			instance.anim = anim;
			return instance;
		}
		
		private AnimWithId() {}
	}
	
}
