package com.github.standobyte.jojo.client.entityrender;

import java.util.Collection;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public abstract class HiddenModelPartsUtil {
	private HiddenModelPartsUtil() {}
	
	public static final String HIDDEN_PREFIX = "hidden.";
	
	public static void initHiddenParts(Model model) {
		var modelParts = ((NamedModelParts) model).jojo_ripples$getAllNamedParts();
		Collection<ModelPart> hiddenCollection = null;
		while (modelParts.hasNext()) {
			var modelPartEntry = modelParts.next();
			String partName = modelPartEntry.getKey();
			if (partName.startsWith(HIDDEN_PREFIX)) {
				ModelPart modelPart = modelPartEntry.getValue().get();
				if (hiddenCollection == null) {
					hiddenCollection = ((ModelWithExtraFeatures) model).jojo_ripples$lazyInitHiddenParts();
				}
				hiddenCollection.add(modelPart);
			}
		}
	}

	public static void reset(Model model) {
		Collection<ModelPart> hidden = ((ModelWithExtraFeatures) model).jojo_ripples$getInitiallyHidden();
		if (hidden != null) {
			for (ModelPart modelPart : hidden) {
				modelPart.visible = false;
			}
		}
	}
	
	public static void onAnimate(ModelWithExtraFeatures model, ModelPart modelPart) {
		Collection<ModelPart> hidden = model.jojo_ripples$getInitiallyHidden();
		if (hidden != null  && hidden.contains(modelPart)) {
			modelPart.visible = true;
		}
	}
}
