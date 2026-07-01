package com.github.standobyte.jojo.client.standskin;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityRenderer;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;

import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ModelFromStandSkin {
	public final ResourceModelEntry resourceModel;
	public final boolean loadFromStandSkin;
	@Nullable public ResourceLocation defaultSkinId;
	
	public ModelFromStandSkin(ResourceModelEntry resourceModel, boolean loadFromStandSkin) {
		this.resourceModel = resourceModel;
		this.loadFromStandSkin = loadFromStandSkin;
	}

	public EntityModel<?> getModel(Entity entity) {
		StandSkin standSkin = null;
		if (loadFromStandSkin) {
			standSkin = SimpleEntityRenderer.getStandSkin(entity);
			if (standSkin == null && defaultSkinId != null) {
				standSkin = StandSkinsLoader.getInstance().getDefaultSkin(defaultSkinId);
			}
		}
		return resourceModel.getModel(standSkin);
	}
}
