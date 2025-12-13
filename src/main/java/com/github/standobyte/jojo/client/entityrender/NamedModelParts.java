package com.github.standobyte.jojo.client.entityrender;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.client.model.geom.ModelPart;

public interface NamedModelParts {
	@Nullable Iterator<Map.Entry<String, Optional<ModelPart>>> jojo_ripples$getAllNamedParts();
}
