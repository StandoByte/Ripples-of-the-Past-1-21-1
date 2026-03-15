package com.github.standobyte.jojo.client.entityrender.entities;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowEntity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class StandArrowRenderer<T extends StandArrowEntity> extends ArrowRenderer<T> {
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/entity/stand_arrow.png");

	public StandArrowRenderer(Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(StandArrowEntity standArrowEntity) {
		return TEXTURE;
	}

}
