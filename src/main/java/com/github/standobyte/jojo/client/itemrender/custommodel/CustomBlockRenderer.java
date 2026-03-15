package com.github.standobyte.jojo.client.itemrender.custommodel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CustomBlockRenderer<T extends BlockEntity> extends CustomItemRenderer implements BlockEntityRenderer<T> {
	public T blockEntity;
	
	public CustomBlockRenderer(BlockEntityRendererProvider.Context context, ResourceLocation rotpGeckoModelPath, ResourceLocation modelTexture, T blockEntityForItem) {
		super(context.getBlockEntityRenderDispatcher(), context.getModelSet(), rotpGeckoModelPath, modelTexture);
		this.blockEntity = blockEntityForItem;
	}

	@Override
	public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
			int packedLight, int packedOverlay) {
		renderBlock(blockEntity, partialTick, poseStack, 
				bufferSource, packedLight, packedOverlay, 
				ItemStack.EMPTY, ItemDisplayContext.NONE);
	}
	
	protected void renderBlock(BlockEntity blockEntity, float partialTick, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay,
			ItemStack itemStack, ItemDisplayContext displayContext) {
		poseStack.pushPose();
		if (blockEntity != null) {
			BlockState blockState = blockEntity.getBlockState();
			if (blockState != null) {
				if (blockState.hasProperty(HorizontalDirectionalBlock.FACING)) {
					float f = blockState.getValue(ChestBlock.FACING).toYRot();
					poseStack.translate(0.5F, 0.5F, 0.5F);
					poseStack.mulPose(Axis.YP.rotationDegrees(-f - 180));
					poseStack.translate(-0.5F, -0.5F, -0.5F);
				}
			}
			
		}
		
		super.renderByItem(itemStack, displayContext, poseStack, renderTypeBuffer, light, overlay);
		
		poseStack.popPose();
	}

	@Override
	public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		if (blockEntity != null) {
			render(blockEntity, 0, poseStack, renderTypeBuffer, light, overlay);
		}
		else {
			super.renderByItem(itemStack, displayContext, poseStack, renderTypeBuffer, light, overlay);
		}
	}

}
