package com.github.standobyte.jojo.client.itemrender.custommodel;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.ResourceModelEntry;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.MatrixUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.neoforged.neoforge.client.RenderTypeHelper;

public class CustomItemRenderer extends BlockEntityWithoutLevelRenderer implements ISTERWithEntity {
	protected ResourceModelEntry model;
	protected ResourceLocation texture;
	@Nullable protected LivingEntity entity;

	public CustomItemRenderer(Minecraft mc, 
			ResourceLocation rotpGeckoModelPath, ResourceLocation modelTexture) {
		this(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels(), 
				rotpGeckoModelPath, modelTexture);
	}

	public CustomItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet, 
			ResourceLocation rotpGeckoModelPath, ResourceLocation modelTexture) {
		super(blockEntityRenderDispatcher, entityModelSet);
		this.model = RotpGeckoModelLoader.getInstance().getModelContainer(rotpGeckoModelPath);
		this.texture = modelTexture;
	}

	@Override
	public void setEntity(@Nullable LivingEntity entity) {
		this.entity = entity;
	}

	@Override
	public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		ModelPart modelRoot = model.modelRoot;
		if (modelRoot != null) {
			poseStack.pushPose();
			poseStack.scale(-1.0F, -1.0F, 1.0F);
			poseStack.translate(-0.5, -1.5, 0.5);
			doRender(itemStack, displayContext, poseStack, renderTypeBuffer, light, overlay);
			poseStack.popPose();
		}
		else {
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			BakedModel missingModel = itemRenderer.getItemModelShaper().getModelManager().getMissingModel();
			VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
					renderTypeBuffer, RenderTypeHelper.getFallbackItemRenderType(itemStack, missingModel, true), 
					false, itemStack.hasFoil());
			itemRenderer.renderModelLists(missingModel, itemStack, light, overlay, poseStack, vertexBuilder);
		}
	}

	protected void doRender(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
				renderTypeBuffer, renderType(texture), false, itemStack.hasFoil());
		model.modelRoot.render(poseStack, vertexBuilder, light, overlay, 0xFFFFFFFF);
	}

	protected RenderType renderType(ResourceLocation texture) {
		return RenderType.entityCutout(texture);
	}



	public static void renderItemNormally(PoseStack poseStack, ItemStack itemStack, ItemDisplayContext displayContext, 
			MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel itemModel) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		boolean cull;
		if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson() && itemStack.getItem() instanceof BlockItem) {
			Block block = ((BlockItem)itemStack.getItem()).getBlock();
			cull = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
		} else {
			cull = true;
		}

		for (var model : itemModel.getRenderPasses(itemStack, cull)) {
			for (var rendertype : model.getRenderTypes(itemStack, cull)) {
				VertexConsumer vertexconsumer;
				if (/* ItemRenderer.hasAnimatedTexture(itemStack) */(itemStack.is(ItemTags.COMPASSES) || itemStack.is(Items.CLOCK))
						&& itemStack.hasFoil()) {
					PoseStack.Pose posestack$pose = poseStack.last().copy();
					if (displayContext == ItemDisplayContext.GUI) {
						MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.5F);
					} else if (displayContext.firstPerson()) {
						MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.75F);
					}

					vertexconsumer = ItemRenderer.getCompassFoilBuffer(buffer, rendertype, posestack$pose);
				} else if (cull) {
					vertexconsumer = ItemRenderer.getFoilBufferDirect(buffer, rendertype, true, itemStack.hasFoil());
				} else {
					vertexconsumer = ItemRenderer.getFoilBuffer(buffer, rendertype, true, itemStack.hasFoil());
				}

				itemRenderer.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexconsumer);
			}
		}
	}

}
