package com.github.standobyte.jojo.client.entityrender;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.replace_player_model.CustomPlayerModel;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@SuppressWarnings("rawtypes")
public class LoadedModel {
	@ApiStatus.Internal public LayerDefinition modelDefinition;
	@ApiStatus.Internal public ModelPart rootPart;
	@ApiStatus.Internal public Model _mainModelCached;
	
	@ApiStatus.Internal public PlayerModel asPlayerModel;
	
	public LoadedModel(LayerDefinition modelDefinition) {
		setModelDefinition(modelDefinition);
	}

	
	public void setModelDefinition(LayerDefinition modelDefinition) {
		this.modelDefinition = modelDefinition;
		this.rootPart = modelDefinition.bakeRoot();
	}
	
	@SuppressWarnings("unchecked")
	public <M extends Model> M getMainModel(Function<LayerDefinition, M> modelConstructor) {
		if (_mainModelCached == null && modelDefinition != null) {
			_mainModelCached = modelConstructor.apply(modelDefinition);
		}
		return (M) _mainModelCached;
	}
	
	public void clear() {
		modelDefinition = null;
		rootPart = null;
		_mainModelCached = null;
		
		asPlayerModel = null;
	}
	
	
	
	public PlayerModel asPlayerModel(boolean useHumanoidRigging) {
		if (asPlayerModel != null) return asPlayerModel;

		LayerDefinition playerModel = ModelUtil.copy(modelDefinition);
		asPlayerModel = CustomPlayerModel.createModel(playerModel.bakeRoot(), false, useHumanoidRigging);
		return asPlayerModel;
	}
}
