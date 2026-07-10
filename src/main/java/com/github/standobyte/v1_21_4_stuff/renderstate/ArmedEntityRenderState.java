package com.github.standobyte.v1_21_4_stuff.renderstate;

import com.github.standobyte.v1_21_4_stuff.missingmethods._LivingEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ArmedEntityRenderState extends LivingEntityRenderState {
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public ItemStack rightHandItem = ItemStack.EMPTY;
//    public final ItemStackRenderState rightHandItem = new ItemStackRenderState();
    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public ItemStack leftHandItem = ItemStack.EMPTY;
//    public final ItemStackRenderState leftHandItem = new ItemStackRenderState();

//    public ItemStackRenderState getMainHandItem() {
//        return this.mainArm == HumanoidArm.RIGHT ? this.rightHandItem : this.leftHandItem;
//    }

    public static void extractArmedEntityRenderState(LivingEntity entity, ArmedEntityRenderState reusedState) {
        reusedState.mainArm = entity.getMainArm();
        reusedState.rightHandItem = _LivingEntity.getItemHeldByArm(entity, HumanoidArm.RIGHT);
        reusedState.leftHandItem = _LivingEntity.getItemHeldByArm(entity, HumanoidArm.LEFT);
    }
    
    @Override
    public void clear(float ageInTicks) {
    	super.clear(ageInTicks);
    	this.mainArm = HumanoidArm.RIGHT;
    	this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
    	this.rightHandItem = ItemStack.EMPTY;
    	this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
    	this.leftHandItem = ItemStack.EMPTY;
    }
}
