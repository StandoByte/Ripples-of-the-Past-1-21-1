package com.github.standobyte.jojo.client.entityrender;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@SuppressWarnings("rawtypes")
public class ModelCast {
	@ApiStatus.Internal public LayerDefinition modelDefinition;
	@ApiStatus.Internal public ModelPart rootPart;
	@ApiStatus.Internal public Model mainModelCached;
	
	@ApiStatus.Internal public PlayerModel asPlayerModel;
	
	public ModelCast(LayerDefinition modelDefinition) {
		setModelDefinition(modelDefinition);
	}

	
	public void setModelDefinition(LayerDefinition modelDefinition) {
		this.modelDefinition = modelDefinition;
		this.rootPart = modelDefinition.bakeRoot();
	}
	
	@SuppressWarnings("unchecked")
	public <M extends Model> M getMainModel(Function<LayerDefinition, M> modelConstructor) {
		if (mainModelCached == null && modelDefinition != null) {
			mainModelCached = modelConstructor.apply(modelDefinition);
		}
		return (M) mainModelCached;
	}
	
	public void clear() {
		modelDefinition = null;
		rootPart = null;
		mainModelCached = null;
		
		asPlayerModel = null;
	}
	
	
	
	public PlayerModel asPlayerModel() {
		if (asPlayerModel != null) return asPlayerModel;

		LayerDefinition playerModel = ModelUtil.copy(modelDefinition);
		asPlayerModel = CustomPlayerModel.createModel(playerModel.bakeRoot(), false);
		return asPlayerModel;
	}
}
