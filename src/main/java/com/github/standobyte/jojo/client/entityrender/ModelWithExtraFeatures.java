package com.github.standobyte.jojo.client.entityrender;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.client.model.geom.ModelPart;

public interface ModelWithExtraFeatures {
	Collection<ModelPart> jojo_ripples$lazyInitHiddenParts();
	Collection<ModelPart> jojo_ripples$getInitiallyHidden();
	
	@Nullable ModelPartWithName[] jojo_ripples$getPathToModelPart(String modelPartName);
}
