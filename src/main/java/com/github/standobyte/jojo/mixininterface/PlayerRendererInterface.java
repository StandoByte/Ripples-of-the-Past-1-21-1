package com.github.standobyte.jojo.mixininterface;

import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

import net.minecraft.world.entity.Entity;

public interface PlayerRendererInterface {
	boolean jojo_ripples$isUsingCustomModel();
	void jojo_ripples$setReplacementModel(Entity entity);
	void jojo_ripples$restoreModel();
	
	HumanoidRenderState jojo_ripples$getReusedState();
}
