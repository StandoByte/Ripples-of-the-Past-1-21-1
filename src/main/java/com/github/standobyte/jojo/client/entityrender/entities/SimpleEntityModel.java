package com.github.standobyte.jojo.client.entityrender.entities;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public class SimpleEntityModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart root;

	public SimpleEntityModel(ModelPart root) {
		this.root = root;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.yRot = netHeadYaw * MathUtil.DEG_TO_RAD;
		this.root.xRot = headPitch * MathUtil.DEG_TO_RAD;
	}
}
