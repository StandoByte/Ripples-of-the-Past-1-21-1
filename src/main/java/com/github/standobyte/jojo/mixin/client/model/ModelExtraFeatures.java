package com.github.standobyte.jojo.mixin.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;

import com.github.standobyte.jojo.client.entityrender.ModelWithExtraFeatures;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

@Mixin(Model.class)
public abstract class ModelExtraFeatures implements ModelWithExtraFeatures {
	protected Set<ModelPart> jojo_ripples$hiddenParts;
	
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

}
