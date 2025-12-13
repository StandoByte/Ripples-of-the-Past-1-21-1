package com.github.standobyte.jojo.client.entityanim;

import java.util.Collection;

import com.github.standobyte.jojo.client.entityrender.NamedModelParts;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public interface HiddenModelParts {
	static final String HIDDEN_PREFIX = "hidden#";
	
	Collection<ModelPart> getInitiallyHidden();
	
	default void initHiddenParts(Model model) {
		var modelParts = ((NamedModelParts) model).jojo_ripples$getAllNamedParts();
		var collection = getInitiallyHidden();
		while (modelParts.hasNext()) {
			var modelPartEntry = modelParts.next();
			String partName = modelPartEntry.getKey();
			if (partName.startsWith(HIDDEN_PREFIX)) {
				ModelPart modelPart = modelPartEntry.getValue().get();
				collection.add(modelPart);
			}
		}
	}

	default void reset() {
		for (ModelPart modelPart : getInitiallyHidden()) {
			modelPart.visible = false;
		}
	}
	
	default void onAnimate(ModelPart modelPart) {
		if (getInitiallyHidden().contains(modelPart)) {
			modelPart.visible = true;
		}
	}
}
