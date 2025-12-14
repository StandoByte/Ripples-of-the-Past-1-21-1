package com.github.standobyte.jojo.client.entityrender;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerBendModel;
import com.github.standobyte.jojo.client.entityanim.playerbend.IPlayerLimbBend;
import com.github.standobyte.v1_21_4_stuff.Reminder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidPlayerModel<T extends LivingEntity> extends HumanoidModel<T>/*<S extends HumanoidRenderState>*/ {
	public ModelPart rightArmSlim;
	public ModelPart leftArmSlim;
	
	protected static String[] BASE_HUMANOID_PARTS = new String[] { "head", "body", "right_arm", "left_arm", "right_leg", "left_leg", "right_arm_slim", "left_arm_slim" };
	public static ModelPart addMissingBoneDefinitions(ModelPart root) {
		for (String basePartName : BASE_HUMANOID_PARTS) {
			root.children.putIfAbsent(basePartName, new ModelPart(new ArrayList<>(), new HashMap<>()));
		}
		Reminder.thatHatIsHeadChildNow();
		root/*.getChild("head")*/.children.putIfAbsent("hat", new ModelPart(new ArrayList<>(), new HashMap<>()));
		return root;
	}

	public HumanoidPlayerModel(ModelPart root) {
		super(addMissingBoneDefinitions(root));
		this.rightArmSlim = root.getChild("right_arm_slim");
		this.leftArmSlim = root.getChild("left_arm_slim");
		IPlayerBendModel thisBends = (IPlayerBendModel) this;
		((IPlayerLimbBend) (Object) rightArmSlim).jojo_ripples$setBendBone(thisBends.jojo_ripples$animRightArmBend(), false);
		((IPlayerLimbBend) (Object) leftArmSlim).jojo_ripples$setBendBone(thisBends.jojo_ripples$animLeftArmBend(), false);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(), ImmutableList.of(rightArmSlim, leftArmSlim));
	}

	@Override
	protected ModelPart getArm(HumanoidArm side) {
		return switch (side) {
			case LEFT -> !leftArm.visible && leftArmSlim.visible ? leftArmSlim : leftArm;
			case RIGHT -> !rightArm.visible && rightArmSlim.visible ? rightArmSlim : rightArm;
		};
	}
	
	@Override
	public void setAllVisible(boolean visible) {
		super.setAllVisible(visible);
		this.rightArmSlim.visible = visible;
		this.leftArmSlim.visible = visible;
	}
	
	@Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		if (leftArmSlim != null && leftArm != null) leftArmSlim.copyFrom(leftArm);
		if (rightArmSlim != null && rightArm != null) rightArmSlim.copyFrom(rightArm);
    	super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

	public void setSlim(boolean slim){
		if (slim) {
			leftArm.visible = false;
			rightArm.visible = false;
		}
		else {
			leftArmSlim.visible = false;
			rightArmSlim.visible = false;
		}
	}
}
