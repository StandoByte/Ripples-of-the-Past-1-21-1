package com.github.standobyte.jojo.customobjects;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;

public interface EntityWithStandSkin {
	ResourceLocation getStandType();
	Optional<ResourceLocation> getStandSkin();
}
