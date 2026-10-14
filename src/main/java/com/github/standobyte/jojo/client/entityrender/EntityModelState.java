package com.github.standobyte.jojo.client.entityrender;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

public class EntityModelState {
	public final Map<String, ModelPartState> objects = new HashMap<>();
	
	public void save(Model model) {
		NamedModelParts model_ = (NamedModelParts) model;
		var entryIter = model_.jojo_ripples$getAllNamedParts();
		while (entryIter.hasNext()) {
			Map.Entry<String, Optional<ModelPart>> entry = entryIter.next();
			ModelPartState modelPartState = objects.computeIfAbsent(entry.getKey(), __ -> new ModelPartState());
			modelPartState.enabled = true;
			ModelPart modelPart = entry.getValue().get();
			modelPartState.from(modelPart);
		}
	}
	
	public void apply(Model model) {
		NamedModelParts model_ = (NamedModelParts) model;
		var entryIter = model_.jojo_ripples$getAllNamedParts();
		while (entryIter.hasNext()) {
			Map.Entry<String, Optional<ModelPart>> entry = entryIter.next();
			ModelPartState modelPartState = objects.get(entry.getKey());
			if (modelPartState != null && modelPartState.enabled) {
				ModelPart modelPart = entry.getValue().get();
				modelPartState.to(modelPart);
			}
		}
	}
	
	public void clear() {
		for (ModelPartState modelPartState : objects.values()) {
			modelPartState.enabled = false;
		}
	}
	
	
	public static class ModelPartState {
		public boolean enabled = true;
		
		public float x;
		public float y;
		public float z;
		public float xRot;
		public float yRot;
		public float zRot;
		public float xScale = 1.0F;
		public float yScale = 1.0F;
		public float zScale = 1.0F;
		public boolean visible = true;
		public boolean skipDraw;
		
		public void from(ModelPart modelPart) {
			this.x = modelPart.x;
			this.y = modelPart.y;
			this.z = modelPart.z;
			this.xRot = modelPart.xRot;
			this.yRot = modelPart.yRot;
			this.zRot = modelPart.zRot;
			this.xScale = modelPart.xScale;
			this.yScale = modelPart.yScale;
			this.zScale = modelPart.zScale;
			this.visible = modelPart.visible;
			this.skipDraw = modelPart.skipDraw;
		}
		
		public void to(ModelPart modelPart) {
			modelPart.x = this.x;
			modelPart.y = this.y;
			modelPart.z = this.z;
			modelPart.xRot = this.xRot;
			modelPart.yRot = this.yRot;
			modelPart.zRot = this.zRot;
			modelPart.xScale = this.xScale;
			modelPart.yScale = this.yScale;
			modelPart.zScale = this.zScale;
			modelPart.visible = this.visible;
			modelPart.skipDraw = this.skipDraw;
		}
	}
}
