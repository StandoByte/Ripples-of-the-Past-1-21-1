package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import javax.annotation.Nonnull;

import net.minecraft.client.model.geom.builders.LayerDefinition;

import com.github.standobyte.v1_21_4_stuff.Reminder;
import net.minecraft.resources.ResourceLocation;

public class ClothesModelEntry {
	public final ResourceLocation path;
	public final HumanoidClothesModel model;
//	public final HumanoidClothesModel adultModel;
//	public final HumanoidClothesModel babyModel;
	
	public ClothesModelEntry(ResourceLocation path, LayerDefinition modelDefinition) {
		this.path = path;
		Reminder.toCreateBabyModels();
//		this.adultModel = new HumanoidClothesModel(modelDefinition.bakeRoot());
//		this.babyModel = new HumanoidClothesModel(modelDefinition.apply(HumanoidModel.BABY_TRANSFORMER).bakeRoot());
		this.model = new HumanoidClothesModel(modelDefinition.bakeRoot());
	}
	
	@Nonnull
	public HumanoidClothesModel getModel(/*LivingEntityRenderState renderState*/) {
//		return renderState.isBaby ? babyModel : adultModel;
		return model;
	}
}