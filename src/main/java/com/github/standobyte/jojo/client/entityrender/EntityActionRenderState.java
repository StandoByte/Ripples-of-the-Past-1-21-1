package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.TimelineKeys;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class EntityActionRenderState {
	@Nullable public AnimFramePose staticPose;
	
	@Nullable public ActionAnimIdentifier animId;
	public float time = -1;
	@Nullable public ActionPhase actionPhase;
	public float phaseTime = -1;
	public float phaseCompletion = -1;
	public boolean disableCrouch = false;
	
	public float barragePrecision = 12;
	public float barrageSwingsPerSecond = 80;

	public RotpAnimDefinition anim;
	public float timeSeconds;
	@Nullable public BarrageSwings barrageSwings;


	public static void extract(EntityActionRenderState renderState, LivingEntity performerEntity, @Nullable EntityActionInstance action, float partialTick) {
		if (action != null) {
			renderState.animId = action.ability.getEntityAnim(action);
			renderState.time = action.getAnimFullTicksPassed(partialTick);
			renderState.actionPhase = action.getPhase();
			renderState.phaseTime = action.getAnimPhaseTick(partialTick);
			renderState.phaseCompletion = action.getAnimPhaseRatio(partialTick);
			renderState.disableCrouch = true;
		}
		else {
			renderState.animId = null;
			renderState.time = -1;
			renderState.actionPhase = null;
			renderState.phaseTime = -1;
			renderState.phaseCompletion = -1;
			renderState.disableCrouch = false;
		}
		
		renderState.anim = null;
		renderState.timeSeconds = 0;
		renderState.barrageSwings = null;
	}
	
	public static void setAnim(EntityActionRenderState renderState, LivingEntityRenderState vanillaRenderState, @Nullable LivingEntity entity, 
			RotpAnimDefinition anim, @Nullable BarrageSwings barrageSwings) {
		renderState.anim = anim;
		renderState.timeSeconds = 0;
		renderState.barrageSwings = barrageSwings;
		if (anim != null) {
			renderState.timeSeconds = anim.getAnimTime(renderState);
			if (barrageSwings != null) {
				String barrageType = anim.instructionTimelines.getStringTimelineVal(TimelineKeys.BARRAGE, renderState.timeSeconds);
				barrageSwings.frameStandBarrage(Minecraft.getInstance(), anim, barrageType, renderState.timeSeconds, vanillaRenderState);
				
				if (entity instanceof StandEntity stand) {
					renderState.barrageSwingsPerSecond = StandStatFormulas.getBarrageHitsPerSecond(stand.getAttackSpeed());
					renderState.barragePrecision = (float) stand.getPrecision();
				}
			}
		}
	}
	
	
	
	
	@Nullable
	public static EntityActionRenderState getFrom(LivingEntityRenderState vanillaRenderState) {
		if (vanillaRenderState instanceof StandEntityRenderState standEntity) {
			return standEntity.action;
		}
		if (vanillaRenderState instanceof RipplesRenderStateExtensionMixin playerMixin) {
			RipplesPlayerRenderState playerExtension = playerMixin.get();
			return playerExtension != null ? playerExtension.entityAction : null;
		}
		return null;
	}
}
