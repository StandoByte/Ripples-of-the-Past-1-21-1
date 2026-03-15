package com.github.standobyte.jojo.mixin.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityanim.pose.PathsToModelParts;
import com.github.standobyte.jojo.client.entityrender.ModelPartWithName;
import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(Model.class)
public abstract class ModelExtraFeatures implements ModelWithExtraFeatures {
	protected Set<ModelPart> jojo_ripples$hiddenParts;
	protected Map<String, ModelPartWithName[]> modelPartPaths;
	
	@Override
	public Collection<ModelPart> jojo_ripples$lazyInitHiddenParts() {
		if (jojo_ripples$hiddenParts == null) {
			jojo_ripples$hiddenParts = new HashSet<>();
		}
		return jojo_ripples$hiddenParts;
	}
	
	@Override
	public Collection<ModelPart> jojo_ripples$getInitiallyHidden() {
		return jojo_ripples$hiddenParts;
	}
	
	@Override
	public ModelPartWithName[] jojo_ripples$getPathToModelPart(String modelPartName) {
		if (modelPartPaths == null) {
			modelPartPaths = PathsToModelParts.make("root", ((Model_1_21_2plus) this).jojo_ripples$root());
		}
		return modelPartPaths.get(modelPartName);
	}

}
