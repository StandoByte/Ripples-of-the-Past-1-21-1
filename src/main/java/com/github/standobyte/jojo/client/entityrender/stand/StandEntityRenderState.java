package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

public class StandEntityRenderState extends HumanoidRenderState {
	public StandSkin skin;
	public final EntityActionRenderState action = new EntityActionRenderState();
	public int tint = -1;
	public float alpha = 1;
	public HumanoidPart[] visibleParts = HumanoidPart.ALL;
	public boolean mayObstructView;
	public boolean doScalingFromStandSkin;

    public static void extractStandRenderState(StandEntity entity, StandEntityRenderState reusedState, float partialTick) {}
}
