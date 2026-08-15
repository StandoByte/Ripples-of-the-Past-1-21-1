package com.github.standobyte.jojo.mixininterface;

import java.util.List;

import com.github.standobyte.jojo.client.firstperson.FirstPersonModelLayer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface LivingRendererLayers<E extends LivingEntity, M extends EntityModel<E>> {
	List<FirstPersonModelLayer> jojo_ripples$firstPersonHandLayers();
	List<RenderLayer<E, M>> jojo_ripples$allLayers();
	void jojoRipples$setOnlyRenderFirstPersonLayers();
	boolean jojo_ripples$shouldRenderLayer(RenderLayer layer, Entity livingEntity);
}
