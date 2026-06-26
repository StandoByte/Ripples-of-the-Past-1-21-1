package com.github.standobyte.jojo.client.entityrender.parsemodel.loader;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.LoadedModel;
import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityModel;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.v1_21_4_stuff.missingmethods.Model_1_21_2plus;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ResourceModelEntry {
	public final ResourceLocation modelPath;
	public LoadedModel _model;
	public Function<LayerDefinition, ? extends Model> _modelConstructor = layer -> {
		ModelPart root = layer.bakeRoot();
		Model model = new SimpleEntityModel<>(root);
		Model_1_21_2plus _this = (Model_1_21_2plus) model;
		_this.jojo_ripples$initRoot(root);
		return model;
	};
	
	public ResourceModelEntry(ResourceLocation modelPath) {
		this.modelPath = modelPath;
	}
	
	public <T extends Entity> void rendererInit(Function<ModelPart, ? extends Model> modelClass) {
		_modelConstructor = (LayerDefinition modelDefinition) -> modelClass.apply(modelDefinition.bakeRoot());
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <M extends Model> M getModel(@Nullable StandSkin standSkin) {
		if (standSkin != null) {
			M modelFromSkin = (M) standSkin.getModel(this.modelPath, _modelConstructor);
			if (modelFromSkin != null) {
				return modelFromSkin;
			}
		}
		
		M modelFromResource = (M) this.getModel(_modelConstructor);
		return modelFromResource;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <M extends Model> M getModel() {
		return (M) this.getModel(_modelConstructor);
	}

	@ApiStatus.Internal
	public <M extends Model> M getModel(Function<LayerDefinition, M> modelConstructor) {
		return (_model != null) ? _model.getMainModel(modelConstructor) : null;
	}
	
	public LayerDefinition getModelDefinition() {
		return (_model != null) ? _model.modelDefinition : null;
	}
	
	public ModelPart getModelRoot() {
		return (_model != null) ? _model.rootPart : null;
	}
	
	
	@ApiStatus.Internal
	public void clear() {
		if (_model != null) _model.clear();
	}
	
	@ApiStatus.Internal
	public void onModelLoad(@Nonnull LayerDefinition newModelLoaded) {
		_model = new LoadedModel(newModelLoaded);
	}
	
}
