package com.github.standobyte.jojo.client.entityrender.entities;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.mc.entity.util.EntityWithStandSkin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SimpleEntityRenderer<T extends Entity> extends EntityRenderer<T> {
	protected EntityModel<T> hardcodedModel;
	
	protected ResourceModelEntry resourceModel;
	
	protected ResourceLocation texPath;
	protected boolean texFromStandSkin;
	protected boolean modelFromStandSkin;

	public SimpleEntityRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager);
	}
	
	public SimpleEntityRenderer<T> initTexture(ResourceLocation texPath, boolean loadFromStandSkin) {
		this.texPath = texPath;
		this.texFromStandSkin = loadFromStandSkin;
		return this;
	}
	
	public SimpleEntityRenderer<T> initModel(EntityModel<T> model) {
		this.hardcodedModel = model;
		return this;
	}
	
	public SimpleEntityRenderer<T> initResourceModel(ResourceLocation modelPath, 
			Function<ModelPart, EntityModel<T>> modelClass, boolean loadFromStandSkin) {
		this.resourceModel = RotpGeckoModelLoader.getInstance().getModelContainer(modelPath);
		this.modelFromStandSkin = loadFromStandSkin;
		this.resourceModel.rendererInit(modelClass);
		return this;
	}
	

	protected EntityModel<T> getEntityModel(T entity) {
		if (resourceModel != null) {
			EntityModel<T> modelFromResource = resourceModel.getModel(modelFromStandSkin ? SimpleEntityRenderer.getStandSkin(entity) : null);
			if (modelFromResource != null) {
				return modelFromResource;
			}
		}
		
		return this.hardcodedModel;
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		if (texFromStandSkin) {
			StandSkin standSkin = getStandSkin(entity);
			if (standSkin != null) {
				return standSkin.getTexture(texPath);
			}
		}
		
		return texPath;
	}
	
	@Nullable
	public static StandSkin getStandSkin(Entity entity) {
		if (entity instanceof EntityWithStandSkin entityWithSkin) {
			ResourceLocation standType = entityWithSkin.getStandType();
			Optional<ResourceLocation> standSkinName = entityWithSkin.getStandSkin();
			return StandSkinsLoader.getInstance().getSkinFromId(standType, standSkinName);
		}
		return null;
	}

	@Override
	public void render(T entity, float yRotation, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (shouldRender(entity)) {
			EntityModel<T> model = getEntityModel(entity);
			if (model == null) return;
			poseStack.pushPose();
			poseStack.scale(1.0F, -1.0F, -1.0F);
			float xRotation = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
			offsetFromDimensions(entity, model, partialTick, poseStack, buffer, packedLight);
			rotateModel(model, entity, partialTick, yRotation, xRotation, poseStack);
			doRender(entity, model, partialTick, poseStack, buffer, packedLight);
			poseStack.popPose();
			super.render(entity, yRotation, partialTick, poseStack, buffer, packedLight);
		}
	}

	protected boolean shouldRender(T entity) {
		return !entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player);
	}

	protected void rotateModel(EntityModel<T> model, T entity, float partialTick, float yRotation, float xRotation, PoseStack poseStack) {
		model.setupAnim(entity, 0, 0, entity.tickCount + partialTick, yRotation, xRotation);
	}
	
	// this works well for projectiles, just make sure the pivot of the root model part is at (0; 24; 0)
	protected void offsetFromDimensions(T entity, EntityModel<T> model, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float height = entity.getBbHeight();
		poseStack.translate(0, -height / 2, 0);
	}

	protected void doRender(T entity, EntityModel<T> model, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		renderModel(entity, model, partialTick, poseStack, buffer.getBuffer(model.renderType(getTextureLocation(entity))), packedLight);
	}

	protected void renderModel(T entity, EntityModel<T> model, float partialTick, PoseStack poseStack, VertexConsumer vertexBuilder, int packedLight) {
		model.renderToBuffer(poseStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY, BlitFloat.NO_TINT);
	}

}
