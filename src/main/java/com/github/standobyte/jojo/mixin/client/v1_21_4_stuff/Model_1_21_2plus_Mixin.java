package com.github.standobyte.jojo.mixin.client.v1_21_4_stuff;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.mixin.client.model.ModelAnimOptimization;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(EntityModel.class)
public abstract class Model_1_21_2plus_Mixin implements Model_1_21_2plus {
	private ModelPart jojo_ripples$root;
    private List<ModelPart> jojo_ripples$allParts;
	
	@Override
	public void jojo_ripples$initRoot(ModelPart root) {
		this.jojo_ripples$root = root;
		if (root != null) {
			((ModelAnimOptimization) (Object) this).jojo_ripples$initModelPartsCache(root);
			jojo_ripples$allParts = root.getAllParts().toList();
		}
	}
	
	@Override
	public ModelPart jojo_ripples$root() {
		return jojo_ripples$root;
	}
	
	@Override
	public List<ModelPart> jojo_ripples$allParts() {
		return jojo_ripples$allParts;
	}
}
