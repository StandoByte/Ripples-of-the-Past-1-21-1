package com.github.standobyte.jojo.client.entityrender.parsemodel.loader;

import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.standskin.StandSkin;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ResourceModelEntry {
	public final ResourceLocation modelPath;
	@Nullable public LayerDefinition modelDefinition;
	@Nullable public ModelPart modelRoot;
	@Nullable public Model model;
	
	@ApiStatus.Internal
	public Function<LayerDefinition, ? extends Model> modelConstructor;
	
	public ResourceModelEntry(ResourceLocation modelPath) {
		this.modelPath = modelPath;
	}
	
	public <T extends Entity> void rendererInit(Function<ModelPart, ? extends Model> modelClass) {
		this.modelConstructor = (LayerDefinition modelDefinition) -> modelClass.apply(modelDefinition.bakeRoot());
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <M extends Model> M getModel(@Nullable StandSkin standSkin) {
		if (standSkin != null) {
			M modelFromSkin = (M) standSkin.getModel(this.modelPath, this.modelConstructor);
			if (modelFromSkin != null) {
				return modelFromSkin;
			}
		}
		
		M modelFromResource = (M) this.getModel(this.modelConstructor);
		return modelFromResource;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <M extends Model> M getModel() {
		return (M) this.getModel(this.modelConstructor);
	}

	@SuppressWarnings("unchecked")
	@ApiStatus.Internal
	public <M extends Model> M getModel(Function<LayerDefinition, M> modelConstructor) {
		if (model == null && modelDefinition != null) {
			this.model = modelConstructor.apply(modelDefinition);
		}
		return (M) model;
	}
	
	
	@ApiStatus.Internal
	public void reset() {
		this.modelDefinition = null;
		this.modelRoot = null;
		this.model = null;
	}
	
	@ApiStatus.Internal
	public void onModelLoad(@Nonnull LayerDefinition newModelLoaded) {
		this.modelDefinition = newModelLoaded;
		this.modelRoot = modelDefinition.bakeRoot();
	}
	
}
