package com.github.standobyte.jojo.mixin.client.firstperson;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.client.firstperson.FirstPersonModelLayer;
import com.github.standobyte.jojo.mixininterface.LivingRendererLayers;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements LivingRendererLayers<T, M> {
	protected final List<FirstPersonModelLayer> jojo_ripples$firstPersonHandLayers = new ArrayList<>();

	@Inject(method = "addLayer", at = @At("TAIL"))
	public void jojo_ripples$onAddLayer(RenderLayer<T, M> layer, CallbackInfoReturnable<Boolean> ci) {
		if (layer instanceof FirstPersonModelLayer firstPersonLayer) {
			jojo_ripples$firstPersonHandLayers.add(firstPersonLayer);
		}
	}
	
	@Override 
	public List<FirstPersonModelLayer> jojo_ripples$firstPersonHandLayers() { return jojo_ripples$firstPersonHandLayers; }
}
