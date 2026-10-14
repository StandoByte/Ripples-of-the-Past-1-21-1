package com.github.standobyte.jojo.client.entityrender.stand;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class StandGlowLayer<T extends StandEntity, S extends StandEntityRenderState, M extends StandEntityModel<T, S>> extends StandModelLayerRenderer<T, S, M> {
    public StandGlowLayer(RenderLayerParent<T, M> entityRenderer) {
        super(entityRenderer);
    }

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, S renderState,
			float headYRotation, float headXRotation) {
		M parentModel = getParentModel();
        if (renderState.isInvisibleToPlayer || parentModel == null) return;
        StandSkin skin = renderState.skin;
        if (skin == null) return;
        ResourceLocation glowTexture = skin.getStandGlowTexture();
        if (glowTexture == null) return;

        RenderType renderType = standRenderer.getRenderType(glowTexture, renderState);
        if (renderType != null) {
            parentModel.renderToBuffer(poseStack, buffer.getBuffer(renderType), ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY, -1);
        }
	}

}