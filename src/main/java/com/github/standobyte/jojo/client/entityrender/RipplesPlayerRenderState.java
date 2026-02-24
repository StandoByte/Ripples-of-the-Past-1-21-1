package com.github.standobyte.jojo.client.entityrender;

import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

import net.minecraft.world.entity.LivingEntity;

public class RipplesPlayerRenderState {
	public EntityActionRenderState entityAction = new EntityActionRenderState();

	public static void extract(LivingEntity entity, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState, 
			float partialTick/*, ItemModelResolver itemModelResolver*/) {
		EntityActionRenderState.extract(modRenderState.entityAction, entity, partialTick);
		if (((AnimatedEntity) entity).jojo_ripples$crouchDisabled()) vanillaRenderState.isCrouching = false;
	}
	
	public static interface RipplesRenderStateExtensionMixin {
		public RipplesPlayerRenderState get(); 
	}
}
