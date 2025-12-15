package com.github.standobyte.v1_21_4_stuff.renderstate;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerBendModel;
import com.github.standobyte.jojo.client.entityrender.HiddenModelPartsUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;

// render states are very cool, i wish they were on this version too
public class EntityRenderState {
    public double x;
    public double y;
    public double z;
    public float ageInTicks;
    public float boundingBoxWidth;
    public float boundingBoxHeight;
    public float eyeHeight;
    public double distanceToCameraSq;
    public boolean isInvisible;
    public boolean isDiscrete;
    public boolean displayFireAnimation;
    @Nullable
    public Vec3 passengerOffset;
    @Nullable
    public Component nameTag;
    @Nullable
    public Vec3 nameTagAttachment;
    @Nullable
    public EntityRenderState.LeashState leashState;
    public float partialTick;

    public static class LeashState {
        public Vec3 offset = Vec3.ZERO;
        public Vec3 start = Vec3.ZERO;
        public Vec3 end = Vec3.ZERO;
        public int startBlockLight = 0;
        public int endBlockLight = 0;
        public int startSkyLight = 15;
        public int endSkyLight = 15;
    }
    
    public static void extract(Entity entity, EntityRenderState reusedState, 
    		EntityRenderer<?> renderer, EntityRenderDispatcher entityRenderDispatcher, float partialTick) {
    	reusedState.x = Mth.lerp((double)partialTick, entity.xOld, entity.getX());
    	reusedState.y = Mth.lerp((double)partialTick, entity.yOld, entity.getY());
    	reusedState.z = Mth.lerp((double)partialTick, entity.zOld, entity.getZ());
    	reusedState.isInvisible = entity.isInvisible();
    	reusedState.ageInTicks = (float)entity.tickCount + partialTick;
    	reusedState.boundingBoxWidth = entity.getBbWidth();
    	reusedState.boundingBoxHeight = entity.getBbHeight();
    	reusedState.eyeHeight = entity.getEyeHeight();
		reusedState.passengerOffset = null;

//    	reusedState.distanceToCameraSq = entityRenderDispatcher.distanceToSqr(entity);
//    	boolean flag = reusedState.distanceToCameraSq < 4096.0 && renderer.shouldShowName(entity/*, reusedState.distanceToCameraSq*/);
//    	if (flag) {
////    		reusedState.nameTag = renderer.getNameTag(entity);
    		reusedState.nameTag = entity.getDisplayName();
    		reusedState.nameTagAttachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, getYRot(entity, partialTick));
//    	} else {
//    		reusedState.nameTag = null;
//    	}

    	reusedState.isDiscrete = entity.isDiscrete();
//    	Entity leashHolder = leashHolder instanceof Leashable leashable ? leashable.getLeashHolder() : null;
//    	if (leashHolder != null) {
//    		float f = leashHolder.getPreciseBodyRotation(partialTick) * (float) (Math.PI / 180.0);
//    		Vec3 vec3 = leashHolder.getLeashOffset(partialTick).yRot(-f);
//    		BlockPos blockpos1 = BlockPos.containing(leashHolder.getEyePosition(partialTick));
//    		BlockPos blockpos = BlockPos.containing(leashHolder.getEyePosition(partialTick));
//    		if (reusedState.leashState == null) {
//    			reusedState.leashState = new EntityRenderState.LeashState();
//    		}
//
//    		EntityRenderState.LeashState entityrenderstate$leashstate = reusedState.leashState;
//    		entityrenderstate$leashstate.offset = vec3;
//    		entityrenderstate$leashstate.start = leashHolder.getPosition(partialTick).add(vec3);
//    		entityrenderstate$leashstate.end = leashHolder.getRopeHoldPosition(partialTick);
//    		entityrenderstate$leashstate.startBlockLight = renderer.getBlockLightLevel(leashHolder, blockpos1);
//    		entityrenderstate$leashstate.endBlockLight = entityRenderDispatcher.getRenderer(leashHolder).getBlockLightLevel(leashHolder, blockpos);
//    		entityrenderstate$leashstate.startSkyLight = leashHolder.level().getBrightness(LightLayer.SKY, blockpos1);
//    		entityrenderstate$leashstate.endSkyLight = leashHolder.level().getBrightness(LightLayer.SKY, blockpos);
//    	} else {
    		reusedState.leashState = null;
//    	}

    	reusedState.displayFireAnimation = entity.displayFireAnimation();

    	reusedState.partialTick = partialTick;
    }


    public static float getXRot(Entity entity, float partialTick) {
        return partialTick == 1.0F ? entity.getXRot() : Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
    }

    public static float getYRot(Entity entity, float partialTick) {
        return partialTick == 1.0F ? entity.getYRot() : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
    }
    
    
    public static void resetPose(EntityModel<?> model) {
        for (ModelPart modelpart : ((Model_1_21_2plus) model).jojo_ripples$allParts()) {
            modelpart.resetPose();
        }
        if (model instanceof IPlayerBendModel playerBendModel) {
        	playerBendModel.jojo_ripples_v1_21_1$onResetPose();
        }
        HiddenModelPartsUtil.reset(model);
    }

}
