package com.github.standobyte.jojo.client.entityrender.entities;

import com.github.standobyte.jojo.customobjects.entity_projectile.ThrownBlockEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDBlockBulletRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ThrownBlockRenderer extends EntityRenderer<ThrownBlockEntity> {
	private final BlockRenderDispatcher dispatcher;

	public ThrownBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.dispatcher = context.getBlockRenderDispatcher();
	}

	private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	@Override
	public ResourceLocation getTextureLocation(ThrownBlockEntity pEntity) {
		BlockState block = pEntity.getBlock();
		if (block != null) {
			TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
					.getBlockModel(block).getParticleIcon(ModelData.EMPTY);
			if (sprite != null) {
				ResourceLocation texture = CrazyDBlockBulletRenderer.getSpriteTexture(sprite);
				if (texture != null) {
					return texture;
				}
			}
		}
		return BLOCK_ATLAS;
	}

	@Override
	public void render(ThrownBlockEntity entity, float yRotation, float partialTick, 
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		Minecraft mc = Minecraft.getInstance();
		if (!entity.isInvisible() || !entity.isInvisibleTo(mc.player)) {
			BlockState blockState = entity.getBlock();
			if (blockState != null) {
				if (blockState.getRenderShape() == RenderShape.MODEL) {
					Level level = entity.level();
					if (blockState != level.getBlockState(entity.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
						poseStack.pushPose();
						BlockPos blockpos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
						poseStack.translate(-0.5, 0.0, -0.5);
						BakedModel model = this.dispatcher.getBlockModel(blockState);
						BlockPos startPos = entity.getOriginBlockPos().orElse(BlockPos.ZERO);
						for (RenderType renderType : model.getRenderTypes(blockState, RandomSource.create(blockState.getSeed(startPos)), ModelData.EMPTY))
							this.dispatcher
							.getModelRenderer()
							.tesselateBlock(
									level,
									this.dispatcher.getBlockModel(blockState),
									blockState,
									blockpos,
									poseStack,
									buffer.getBuffer(RenderTypeHelper.getMovingBlockRenderType(renderType)),
									false,
									RandomSource.create(),
									blockState.getSeed(startPos),
									OverlayTexture.NO_OVERLAY,
									ModelData.EMPTY,
									renderType
									);
						poseStack.popPose();
						super.render(entity, yRotation, partialTick, poseStack, buffer, packedLight);
					}
				}
			}
		}
	}

}
