package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Vector3f;

import com.github.standobyte.jojo.client.entityanim.AnimFramePose.ModelPartFrame;
import com.github.standobyte.jojo.client.entityanim.action.AnimActionPhase;
import com.github.standobyte.jojo.client.entityanim.action.AnimInstructionTimelines;
import com.github.standobyte.jojo.client.entityanim.action.AnimObjTimeline;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery;
import com.github.standobyte.jojo.client.entityanim.molang.KeyframeQuery;
import com.github.standobyte.jojo.client.entityanim.playerbend.PlayerModelBends;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.HiddenModelPartsUtil;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.java.OptionalFloat;
import com.github.standobyte.v1_21_4_stuff.OldPlayerModelJank;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class RotpAnimDefinition {
	public final float lengthInSeconds;
	public final OptionalFloat loopBackTo;
	protected final Map<String, List<AnimationChannel>> boneAnimations;
	protected final List<KeyframeQuery> queries;
	public final AnimInstructionTimelines instructionTimelines;
	@Nullable public List<AnimFramePose> coolPoses;
	
//	public float animTime;
	
	public RotpAnimDefinition(float lengthInSeconds, OptionalFloat loopBackTo, Map<String, List<AnimationChannel>> boneAnimations, 
			@Nullable List<KeyframeQuery> queries, AnimInstructionTimelines instructionTimelines, @Nullable FloatList coolPoseTimestamps) {
		this.lengthInSeconds = lengthInSeconds;
		this.loopBackTo = loopBackTo;
		this.boneAnimations = boneAnimations;
		this.queries = queries != null ? queries : Collections.emptyList();
		this.instructionTimelines = instructionTimelines;
		if (coolPoseTimestamps != null) {
			this.coolPoses = new ArrayList<>(coolPoseTimestamps.size());
			FloatListIterator iter = coolPoseTimestamps.iterator();
			while (iter.hasNext()) {
				float timestamp = iter.nextFloat();
				AnimFramePose frame = calcAnimPose(null, timestamp, 1);
				frame = frame.deepCopy();
				coolPoses.add(frame);
			}
		}
	}


	public AnimFramePose calcAnimPose(LivingEntityRenderState renderState, float seconds, float animSpeed) {
		evaluateQueries(renderState);
		AnimFramePose frame = AnimFramePose.reused.clear();
		for (Map.Entry<String, List<AnimationChannel>> entry : boneAnimations.entrySet()) {
			ModelPartFrame modelPartPose = frame.getForModelPart(entry.getKey());
			for (AnimationChannel tf : entry.getValue()) {
				Vector3f vec = calcVec(this, tf, seconds, animSpeed);
				modelPartPose.set(vec, tf.target());
			}
		}
		return frame;
	}
	
	public static void animate(Model model, AnimFramePose frame) {
		HumanoidModel<?> humanoidModelCast = model instanceof HumanoidModel __ ? __ : null;
		Model_1_21_2plus backportModelCast = (Model_1_21_2plus) model;
		ModelWithExtraFeatures rotpModelCast = (ModelWithExtraFeatures) model;
		
		for (var modelPartEntry : frame.pose.entrySet()) {
			String modelPartName = modelPartEntry.getKey();
			ModelPart modelPart = getModelPart(modelPartName, model, humanoidModelCast, backportModelCast);
			if (modelPart != null) {
				HiddenModelPartsUtil.onAnimate(rotpModelCast, modelPart);
				modelPartEntry.getValue().apply(modelPart);
			}
		}
		
		if (humanoidModelCast != null) {
			OldPlayerModelJank._onAnimate(humanoidModelCast);
		}
	}
	

	public static ModelPart getModelPart(String animBoneName, Model model, @Nullable HumanoidModel<?> humanoidModelCast, @Nullable Model_1_21_2plus rotpModelCast) {
		if (humanoidModelCast != null) {
			ModelPart playerModelPart = PlayerModelBends.getModelPartForPlayerAnim(humanoidModelCast, animBoneName);
			if (playerModelPart != null) {
				return playerModelPart;
			}
		}
		if (rotpModelCast != null) {
			Optional<ModelPart> modelPart = rotpModelCast.jojo_ripples$getAnyDescendantWithName(animBoneName);
			if (modelPart.isPresent()) return modelPart.get();
		}
		return null;
	}
	

	public void animate(Model model, LivingEntityRenderState renderState, float seconds, float animSpeed) {
		AnimFramePose frame = calcAnimPose(renderState, seconds, animSpeed);
		animate(model, frame);
	}

	@Deprecated
	public void animateVanillaPlayer(HumanoidModel<?> humanoidModel, LivingEntityRenderState renderState, float seconds, float animSpeed) {
		animate(humanoidModel, renderState, seconds, animSpeed);
	}
	
	
	/**
	 * @return action anim time in seconds
	 */
	public float getAnimTime(EntityActionRenderState entityAction) {
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
	

	protected static final Vector3f TEMP = new Vector3f();
	
	public static Vector3f calcVec(RotpAnimDefinition anim, AnimationChannel tf, float seconds, float animSpeed) {
		Keyframe[] keyframes = tf.keyframes();
		anim.lerpKeyframes(keyframes, seconds, animSpeed);
		if (tf.target() == AnimationChannel.Targets.ROTATION) {
			TEMP.mul(MathUtil.DEG_TO_RAD);
		}
		else if (tf.target() == AnimationChannel.Targets.POSITION) {
			TEMP.mul(1, -1, 1);
		}
		else if (tf.target() == AnimationChannel.Targets.SCALE) {
			TEMP.add(-1, -1, -1);
		}
		return TEMP;
	}
	
	public Vector3f lerpKeyframes(Keyframe[] keyframes, float seconds, float animSpeed) {
		int i = Math.max(0, Mth.binarySearch(0, keyframes.length, index -> seconds <= keyframes[index].timestamp()) - 1);
		int j = Math.min(keyframes.length - 1, i + 1);
		Keyframe keyframe = keyframes[i];
		Keyframe keyframe2 = keyframes[j];
		float h = seconds - keyframe.timestamp();
		float k = j != i ? Mth.clamp(h / (keyframe2.timestamp() - keyframe.timestamp()), 0.0f, 1.0f) : 0.0f;
		keyframe2.interpolation().apply(TEMP, k, keyframes, i, j, animSpeed);
		return TEMP;
	}
	
	
	private void evaluateQueries(@Nullable LivingEntityRenderState renderState) {
		if (renderState != null) 	AnimMolangQuery.instance.fillContext(renderState);
		else						AnimMolangQuery.instance.reset();
		queries.forEach(KeyframeQuery::evaluate);
	}
	

	public static class TimelineKeys {
		public static final String BARRAGE = "barrage";
	}
	
	
	public static class Builder {
		protected float length;
		protected final Map<String, List<AnimationChannel>> animationByBone = Maps.newHashMap();
		protected OptionalFloat loopBackTo = OptionalFloat.empty();
		protected List<KeyframeQuery> queries = null;
		protected final AnimInstructionTimelines instructions = new AnimInstructionTimelines();
		protected FloatList coolPoses;
		
		public Builder(float lengthInSeconds) {
			this.length = lengthInSeconds;
		}

		public RotpAnimDefinition.Builder looping() {
			return looping(0);
		}

		public RotpAnimDefinition.Builder looping(float loopBackToSec) {
			this.loopBackTo = OptionalFloat.of(loopBackToSec);
			return this;
		}

		public RotpAnimDefinition.Builder addAnimation(String bone, AnimationChannel animationChannel) {
			this.animationByBone.computeIfAbsent(bone, p_329694_ -> new ArrayList<>()).add(animationChannel);
			return this;
		}

		public RotpAnimDefinition.Builder addExpressionQuery(KeyframeQuery query) {
			if (queries == null) queries = new ArrayList<>();
			if (!query.isNumericLiteral()) {
				queries.add(query);
			}
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
		
		public RotpAnimDefinition.Builder addCoolPoseTimestamp(float time) {
			if (coolPoses == null) {
				coolPoses = new FloatArrayList();
			}
			coolPoses.add(time);
			return this;
		}
		
		public RotpAnimDefinition build() {
			instructions.onFinishedParsing();
			RotpAnimDefinition anim = new RotpAnimDefinition(length, loopBackTo, animationByBone, queries, instructions, coolPoses);
			return anim;
		}
	}
	
}
