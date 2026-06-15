package com.github.standobyte.jojo.client.firstperson;

import java.util.List;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public interface LivingLayersAccess<E extends LivingEntity, M extends EntityModel<E>> {
	List<FirstPersonModelLayer> jojo_ripples$firstPersonHandLayers();
	List<RenderLayer<E, M>> jojo_ripples$allLayers();
}
