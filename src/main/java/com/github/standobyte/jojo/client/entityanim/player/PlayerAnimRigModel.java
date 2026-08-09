package com.github.standobyte.jojo.client.entityanim.player;

import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityModel;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class PlayerAnimRigModel extends SimpleEntityModel<Entity> {
	public ModelPart rightArm;
	public ModelPart leftArm;
	public ModelPart rightLeg;
	public ModelPart leftLeg;

	public PlayerAnimRigModel(ModelPart root) {
		super(root);
		Model_1_21_2plus _this = (Model_1_21_2plus) this;
		this.rightArm = _this.jojo_ripples$getAnyDescendantWithName("right_arm").orElse(null);
		this.leftArm = _this.jojo_ripples$getAnyDescendantWithName("left_arm").orElse(null);
		this.rightLeg = _this.jojo_ripples$getAnyDescendantWithName("right_leg").orElse(null);
		this.leftLeg = _this.jojo_ripples$getAnyDescendantWithName("left_leg").orElse(null);
	}

}
