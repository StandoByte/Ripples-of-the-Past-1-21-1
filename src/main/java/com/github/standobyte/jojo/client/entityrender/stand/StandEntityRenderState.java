package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.powersystem.entityaction.ActionOBB;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class StandEntityRenderState extends HumanoidRenderState {
	public ResourceLocation standId;
	public StandSkin skin;
	public final EntityActionRenderState action = new EntityActionRenderState();
	public int tint = -1;
	public float alpha = 1;
	public HumanoidPart[] visibleParts = HumanoidPart.ALL;
	public boolean mayObstructView;
    public double extendablePartLength = 0;
    public Vec3 motionTiltVec = Vec3.ZERO;

    public static void extractStandRenderState(StandEntity entity, StandEntityRenderState reusedState, float partialTick) {
        if (entity.getCurStandAction() instanceof ActionOBB obbToRender && obbToRender.extendableOBB() != null){
            reusedState.extendablePartLength = obbToRender.extendableOBB().getAnimLength(partialTick);
        }
    }
}
