package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.Optional;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

// TODO stand renderer layers (layers suck)
/*
 * glow layer
 * fire on Magician's Red and Silver Chariot's rapier (?)
 */
public class StandModelLayerRenderer<T extends StandEntity, S extends StandEntityRenderState, M extends StandEntityModel<T, S>> extends RenderLayer<T, M> {
	protected final StandEntityRenderer<T, S, M> entityRenderer;
	protected final boolean useParentModel;
	protected final M model;
	protected final ResourceLocation texture;

	public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, M model, ResourceLocation texture) {
		this(entityRenderer, false, model, texture);
	}

	public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, ResourceLocation texture) {
		this(entityRenderer, true, null, texture);
	}

	@SuppressWarnings("unchecked")
	public StandModelLayerRenderer(RenderLayerParent<T, M> entityRenderer, boolean useParentModel, M model, ResourceLocation texture) {
		super(entityRenderer);
		this.entityRenderer = (StandEntityRenderer<T, S, M>) entityRenderer;
		this.model = model;
		this.texture = texture;
		this.useParentModel = useParentModel;
//		if (model != null) {
//			model.setAnimatorSupplier(getParentModel().getGeckoAnimator());
//			if (!useParentModel) {
//				model.afterInit();
//			}
//		}
	}

//	public M getLayerModel(T entity) {
//		return getLayerModel(entity.getStandSkin());
//	}
//
//	public M getLayerModel(Optional<ResourceLocation> standSkin) {
//		if (useParentModel) {
//			return entityRenderer.getModel(standSkin);
//		}
//		if (this.model != null) {
//			M model = CustomResources.getStandModelOverrides().overrideModel(this.model);
//			M skinModel = StandSkinsManager.getInstance().getStandSkin(standSkin).map(
//					skin -> (M) skin.standModels.getOrDefault(model.getModelId(), model)).orElse(model);
//			return skinModel;
//		}
//		return null;
//	}

	public boolean shouldRender(T entity, Optional<ResourceLocation> standSkin) {
		return true;
	}

	public int getPackedLight(int packedLight) {
		return packedLight;
	}

//	public RenderType getRenderType(T entity) {
//		return entityRenderer.getRenderType(entity, getLayerModel(Optional.empty()), getLayerTexture(entity.getStandSkin()));
//	}
//
//	public RenderType getRenderType(T entity, Function<ResourceLocation, RenderType> renderTYPE) {
//		return renderTYPE.apply(getLayerTexture(entity.getStandSkin()));
//	}

	public ResourceLocation getBaseTexture() {
		return texture;
	}

//	public ResourceLocation getLayerTexture(Optional<ResourceLocation> standSkin) {
//		return StandSkinsManager.getInstance()
//				.getRemappedResPath(manager -> manager.getStandSkin(standSkin), texture);
//	}

	@SuppressWarnings("unchecked")
	@Deprecated
	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			T entity, float walkAnimPos, float walkAnimSpeed, float partialTick,
			float ticks, float headYRotation, float headXRotation) {
    	if (RenderStateCrutches.currentEntityRenderState != null) {
    		render(poseStack, buffer, (S) RenderStateCrutches.currentEntityRenderState);
    	}
	}

	public void render(PoseStack poseStack, MultiBufferSource buffer, S renderState) {
//		if (renderType != null && shouldRender(entity, entity.getStandSkin())) {
//			M layerModel = getLayerModel(entity);
//			M parentModel = entityRenderer.getModel(entity);
//			layerModel.idleLoopTickStamp = parentModel.idleLoopTickStamp;
//			entityRenderer.renderLayer(poseStack, buffer.getBuffer(renderType), getPackedLight(packedLight), 
//					entity, walkAnimPos, walkAnimSpeed, partialTick, 
//					ticks, headYRotation, headXRotation, layerModel);
//		}
	}
}
