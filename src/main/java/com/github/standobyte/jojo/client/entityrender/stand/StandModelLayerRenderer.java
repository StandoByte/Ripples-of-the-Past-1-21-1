package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.entityrender.entitylayers.RenderLayer2;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.renderer.entity.RenderLayerParent;

// TODO stand renderer layers (layers suck)
/*
 * fire on Magician's Red and Silver Chariot's rapier (?)
 */
public abstract class StandModelLayerRenderer<T extends StandEntity, S extends StandEntityRenderState, M extends StandEntityModel<T, S>> extends RenderLayer2<T, S, M> {
	protected final StandEntityRenderer<T, S, M> standRenderer;
	
	public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer) {
		super(entityRenderer);
		this.standRenderer = (StandEntityRenderer<T, S, M>) entityRenderer;
	}

}
