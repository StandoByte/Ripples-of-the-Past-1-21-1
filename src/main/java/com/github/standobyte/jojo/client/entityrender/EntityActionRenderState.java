package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public class EntityActionRenderState {
	@Nullable public AnimFramePose pose;
	@Nullable public BarrageSwings barrageSwings;
	
	public static void extract(EntityActionRenderState renderState, LivingEntity entity, float partialTick) {
		AnimatedEntity preCalcPose = (AnimatedEntity) entity;
		renderState.pose = preCalcPose.jojo_ripples$getModelPose(AnimatedEntity.PoseType.FINAL);
		renderState.barrageSwings = getBarrageSwings(entity);
	}

	public static boolean setupModelAnim(HumanoidModel<?> model, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState) {
		if (modRenderState.entityAction.pose != null) {
			RotpAnimDefinition.animate(model, modRenderState.entityAction.pose);
			return true;
		}
		return false;
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
	
	@Nullable
	public static BarrageSwings getBarrageSwings(LivingEntity entity) {
		if (entity instanceof StandEntity stand) {
			return stand.clientStuff.barrageSwings;
		}
		return null;
	}
}
