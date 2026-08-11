package com.github.standobyte.jojo.client.entityanim;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;

import net.minecraft.resources.ResourceLocation;

public class LivingAnimState {
	@Nullable public ResourceLocation animSet;
	@Nullable public ActionAnimIdentifier animId;
	public AnimSource animSource = AnimSource.DEFAULT_ANIM_LOADER;
	
	public float time;
	@Nullable public ActionPhase actionPhase;
	public float phaseTime;
	public float phaseCompletion;
	
	public void reset() {
		this.animSet = null;
		this.animId = null;
		this.animSource = AnimSource.DEFAULT_ANIM_LOADER;
		this.time = -1;
		this.actionPhase = null;
		this.phaseTime = -1;
		this.phaseCompletion = -1;
	}
	
	public static LivingAnimState reusedInstance = new LivingAnimState();
	
	public enum AnimSource {
		DEFAULT_ANIM_LOADER,
		JOJO_POSE_LOADER,
		STAND_SKIN
	}
}
