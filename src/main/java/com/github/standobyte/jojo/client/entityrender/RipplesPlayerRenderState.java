package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimationLoader;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithIdReturn;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

import net.minecraft.client.model.HumanoidModel;
//import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class RipplesPlayerRenderState {
	@Nullable public ResourceLocation animSet;
	public EntityActionRenderState entityAction = new EntityActionRenderState();

	public static void extract(LivingEntity entity, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState, 
			float partialTick/*, ItemModelResolver itemModelResolver*/) {
		EntityActionInstance action = LivingComponentAction.getCurEntityAction(entity);
		EntityActionRenderState.extract(modRenderState.entityAction, entity, action, partialTick);
		
		modRenderState.animSet = null;
		if (action != null) {
			modRenderState.animSet = action.ability.getEntityAnimSet(entity);
		}
		AnimWithIdReturn anim = getPlayerAnim(modRenderState);
		EntityActionRenderState.setAnim(modRenderState.entityAction, vanillaRenderState, entity, 
				anim.animId, anim.anim, null);
		
		if (modRenderState.entityAction.disableCrouch) vanillaRenderState.isCrouching = false;
	}

	public static boolean setupModelAnim(HumanoidModel<?> model, HumanoidRenderState vanillaRenderState, RipplesPlayerRenderState modRenderState) {
		RotpAnimDefinition anim = modRenderState.entityAction.anim;
		float seconds = modRenderState.entityAction.timeSeconds;
//		if (anim == null) {
//			anim = getPlayerAnim(modRenderState);
//			if (anim != null) {
//				seconds = anim.getAnimTime(modRenderState.entityAction);
//			}
//		}
		
		if (anim != null) {
			anim.animate(model, vanillaRenderState, seconds, 1);
			return true;
		}
		
		return false;
	}
	
	public static AnimWithIdReturn getPlayerAnim(RipplesPlayerRenderState modRenderState) {
		if (modRenderState.animSet != null) {
			EntityActionRenderState action = modRenderState.entityAction;
			if (action.animId != null) {
				AnimationSet animSet = AnimationLoader.getInstance().getAnimSet(modRenderState.animSet);
				if (animSet != null) {
					RotpAnimDefinition anim = animSet.getNamedAnim(action.animId);
					return AnimWithIdReturn.with(action.animId, anim);
				}
			}
		}
		return AnimWithIdReturn.with(null, null);
	}
	
	public static interface RipplesRenderStateExtensionMixin {
		public RipplesPlayerRenderState get(); 
	}
}
