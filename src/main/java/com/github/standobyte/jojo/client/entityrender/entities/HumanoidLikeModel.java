package com.github.standobyte.jojo.client.entityrender.entities;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityrender.HumanoidPlayerModel;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidLikeModel<T extends LivingEntity> extends HumanoidModel<T> {
	public final Model_1_21_2plus withRoot;

	public final ModelPart torso_lower;
	public final ModelPart torso_no_arms;
	
	public final ModelPart torso_bend;
	public final ModelPart right_arm_bend;
	public final ModelPart left_arm_bend;
	public final ModelPart right_leg_bend;
	public final ModelPart left_leg_bend;

	/*
	 * ModelPart#getChild, which is called in the HumanoidModel's constructor, throws NoSuchElementException.
	 * To avoid that and initialize the humanoid model parts correctly, we send the dummy to the super constructor.
	 */
	public static final ModelPart DUMMY = HumanoidPlayerModel.addMissingBoneDefinitions(
			new ModelPart(new ArrayList<>(), new HashMap<>()));

	public HumanoidLikeModel(ModelPart root) {
		super(DUMMY);
		((IHumanoidAnimModel) this).jojo_rippes$initDisableBends();
		this.withRoot = (Model_1_21_2plus) this;
		withRoot.jojo_ripples$initRoot(root);
		
		this.torso_lower = withRoot.jojo_ripples$getAnyDescendantWithName("torso_lower").orElse(null);
		this.torso_no_arms = withRoot.jojo_ripples$getAnyDescendantWithName("torso_no_arms").orElse(null);
		this.body = new ModelPart(new ArrayList<>(), new HashMap<>());
		if (torso_lower != null)	this.body.children.put("torso_lower", torso_lower);
		if (torso_no_arms != null)	this.body.children.put("torso_no_arms", torso_no_arms);
		
		this.head = withRoot.jojo_ripples$getAnyDescendantWithName("head").orElse(this.head);
		this.hat = new ModelPart(new ArrayList<>(), new HashMap<>());
		this.rightArm = withRoot.jojo_ripples$getAnyDescendantWithName("right_arm").orElse(this.rightArm);
		this.leftArm = withRoot.jojo_ripples$getAnyDescendantWithName("left_arm").orElse(this.leftArm);
		this.rightLeg = withRoot.jojo_ripples$getAnyDescendantWithName("right_leg").orElse(this.rightLeg);
		this.leftLeg = withRoot.jojo_ripples$getAnyDescendantWithName("left_leg").orElse(this.leftLeg);
		
		this.torso_bend = withRoot.jojo_ripples$getAnyDescendantWithName("torso_bend").orElse(null);
		this.right_arm_bend = rightArm.children.get("right_arm_bend");
		this.left_arm_bend = leftArm.children.get("left_arm_bend");
		this.right_leg_bend = rightLeg.children.get("right_leg_bend");
		this.left_leg_bend = leftLeg.children.get("left_leg_bend");
	}
	
	
	public static void fixLayerModelPartsAfterCopyProperties(HumanoidLikeModel<?> destModel) {
		if (destModel.torso_lower != null) {
			destModel.torso_lower.resetPose();
			destModel.torso_lower.y += 12;
		}
		if (destModel.torso_no_arms != null) {
			destModel.torso_no_arms.resetPose();
			destModel.torso_no_arms.y += 6;
		}
	}
	
}
