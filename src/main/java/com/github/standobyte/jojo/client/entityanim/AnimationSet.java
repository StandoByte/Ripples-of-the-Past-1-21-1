package com.github.standobyte.jojo.client.entityanim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

/**
 * Has some stuff specific to Stands, but it can be used for other entities as well.
 */
public class AnimationSet {
	public final Map<String, AnimVariantsList> namedAnimations;
	@Nullable public RotpAnimDefinition idleAnim;
	@Nullable public List<RotpAnimDefinition> alwaysAnim;
	
	protected AnimationSet(Map<String, AnimVariantsList> namedAnimations) {
		this.namedAnimations = namedAnimations;
		AnimationMirror.doMirroringOnAnimSet(this.namedAnimations);
		this.idleAnim = getNamedAnim(StandEntityRenderer.IDLE_ANIM);
	}

	@Nullable
	public RotpAnimDefinition getNamedAnim(ActionAnimIdentifier animId) {
		AnimVariantsList anims = namedAnimations.get(animId.name);
		if (anims == null) return null;
		return anims.get(animId.index);
	}
	
	@Nullable
	public AnimVariantsList getAnimVariants(String name) {
		return namedAnimations.get(name);
	}
	
	@Nullable
	public RotpAnimDefinition getStandIdleAnim() {
		return idleAnim;
	}
	
	
	public static class Builder {
		protected Map<String, Int2ObjectMap<RotpAnimDefinition>> namedAnimations = new HashMap<>();
		@Nullable protected List<RotpAnimDefinition> alwaysAnim;
		
		public void putNamedAnim(String name, RotpAnimDefinition anim) {
			Pair<String, OptionalInt> enumeratedName = StringUtil.splitIntAtTheEnd(name);
			Int2ObjectMap<RotpAnimDefinition> anims = this.namedAnimations.computeIfAbsent(
					enumeratedName.getFirst(), __ -> new Int2ObjectArrayMap<>());
			anims.put(enumeratedName.getSecond().orElse(0), anim);
		}
		
		public void addAlwaysAnim(RotpAnimDefinition anim) {
			if (alwaysAnim == null) {
				alwaysAnim = new ArrayList<>(1);
			}
			alwaysAnim.add(anim);
		}
		
		public boolean isEmpty() {
			return namedAnimations.isEmpty();
		}
		
		public AnimationSet build() {
			Map<String, AnimVariantsList> anims = this.namedAnimations.entrySet().stream()
					.collect(Collectors.toMap(
							Map.Entry::getKey, 
							entry -> new AnimVariantsList(entry.getValue()
								.int2ObjectEntrySet().stream()
								.sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
								.map(Int2ObjectMap.Entry::getValue)
								.toList())));
			AnimationSet animationSet = new AnimationSet(anims);
			animationSet.alwaysAnim = this.alwaysAnim;
			return animationSet;
		}
	}

	// TODO (stand anims) arms only mode
//	@Override
//	public <T extends StandEntity> boolean poseStand(@Nullable T entity, StandEntityModel<T> model, StandPoseData poseData, 
//			float ticks, float yRotOffsetDeg, float xRotDeg) {
//		model.resetPose(entity);
//		curAnim = null;
//		
//		StandPose standPose = poseData.standPose;
//		if (standPose == StandPose.SUMMON) {
//			List<StandActionAnimation> summonAnims = namedAnimations.get(StandPose.SUMMON.getName());
//			if (summonAnims != null && summonAnims.size() > 0) {
//				StandActionAnimation summonAnim = StandPose.SUMMON.getAnim(summonAnims, entity);
//				
//				if (ticks > summonAnim.anim.lengthInSeconds() * 20) {
//					standPose = StandPose.IDLE;
//					model.setStandPose(standPose, entity);
//				}
//				
//				model.idleLoopTickStamp = ticks;
//				return applyAnim(summonAnim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
//			}
//		}
//		
//		if (standPose != null && standPose != StandPose.IDLE) {
//			model.idleLoopTickStamp = ticks;
//			
//			List<StandActionAnimation> anims = getAnims(entity, standPose);
//			if (anims != null) {
//				StandActionAnimation anim = standPose.getAnim(anims, entity);
//				if (anim != null) {
//					return applyAnim(anim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
//				}
//			}
//		}
//		
//		StandActionAnimation idleAnim = getIdleAnim(entity);
//		if (idleAnim != null) {
//			return applyAnim(idleAnim, entity, model, yRotOffsetDeg, xRotDeg, standPose, poseData);
//		}
//		
//		return exists;
//	}
//	
//	protected <T extends StandEntity> boolean applyAnim(StandActionAnimation anim, @Nullable T entity, 
//			StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg, StandPose standPose, StandPoseData poseData) {
//		curAnim = anim;
//		poseData.edit().standPose(standPose);
//		poseData.standPose.applyAnim(entity, model, anim, yRotOffsetDeg, xRotDeg, poseData);
//		return true;
//		
//	}
//	
//	protected List<StandActionAnimation> getAnims(@Nullable StandEntity entity, StandPose standPose) {
//		String key = standPose.getName();
//		if (entity != null && entity.isArmsOnlyMode()) {
//			String key2 = "armsOnly_" + key;
//			if (namedAnimations.containsKey(key2)) {
//				return namedAnimations.get(key2);
//			}
//		}
//		return namedAnimations.get(key);
//	}
//	
//	
//	
//	
//
//
//	@Override
//	public <T extends StandEntity> void addBarrageSwings(T entity, StandEntityModel<T> model, float ticks) {
//		boolean isBarraging = false;
//		if (curAnim != null) {
//			String barrageType = curAnim.getStringTimelineVal(TimelineKeys.BARRAGE, curAnim.animTime);
//			if (barrageType != null) {
//				isBarraging = BarrageSwings.onBarrageAnim(barrageType, entity, model, curAnim, ticks, curAnim.animTime);
//			}
//		}
//		entity.animWasBarraging = isBarraging;
//	}
//
//	@Override
//	public <T extends StandEntity> void renderBarrageSwings(T entity, StandEntityModel<T> model, float yRotOffsetDeg, float xRotDeg,
//			MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green,
//			float blue, float alpha) {
//		BarrageSwings swings = entity.getBarrageSwings();
//		if (swings != null) {
//			for (BarrageSwing swing : swings.getSwings()) {
//				swing.poseAndRender(entity, model, 
//						matrixStack, buffer, yRotOffsetDeg, xRotDeg, 
//						packedLight, packedOverlay, red, green, blue, alpha);
//			}
//		}
//	}
	
}
