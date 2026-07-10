package com.github.standobyte.jojo.mechanics.jojopose;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;

public class RenderModelWithPose {
	public static HumanoidRenderState reusedState = new HumanoidRenderState();

	public static HumanoidRenderState clearSetupPose(EntityModel<?> model, AnimFramePose pose, float ageInTicks) {
		reusedState.clear(ageInTicks);
		EntityActionRenderState moddedRenderState = reusedState.get().entityAction;
		moddedRenderState.clear();
		moddedRenderState.pose = pose;
		if (model instanceof HumanoidModel humanoidModel) {
			humanoidModel.crouching = false;
		}
        return reusedState;
	}
	
	public static void apply(HumanoidRenderState renderState, EntityModel<?> model) {
        RenderStateCrutches.currentEntityRenderState = renderState; // doesn't seem to be necessary, but JUST in case
		EntityRenderState.resetPose(model);
		((IHumanoidAnimModel) model).jojo_ripples$setupHumanoidAnim(renderState);
	}
	
	public static void afterRender() {
		RenderStateCrutches.currentEntityRenderState = null;
	}
	
}
