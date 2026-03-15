package com.github.standobyte.jojo.client.entityrender.entities;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.github.standobyte.jojo.customobjects.entity_projectile.BlockShardEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDBlockBulletRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class BlockShardRenderer extends EntityRenderer<BlockShardEntity> {

	public BlockShardRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	private static final ResourceLocation BLOCK_ATLAS = ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
	@Override
	public ResourceLocation getTextureLocation(BlockShardEntity pEntity) {
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
	public void render(BlockShardEntity entity, float yRotation, float partialTick, 
			PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		if (!entity.isInvisible() || !entity.isInvisibleTo(Minecraft.getInstance().player)) {
			BlockState blockState = entity.getBlock();
			if (blockState != null) {
				poseStack.pushPose();
				poseStack.scale(-1, -1, 1);
				ModelPart model = getModelPart(entity);
				rotate(entity, model, partialTick);

				// old falling block entity rendering
//				BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//				for (RenderType renderType : RenderType.chunkBufferLayers()) {
//					if (RenderTypeLookup.canRenderInLayer(blockState, renderType)) {
//						ForgeHooksClient.setRenderLayer(renderType);
////						blockRenderer.getModelPart().tesselateBlock(entity.level, 
////								blockRenderer.getBlockModel(blockState), blockState, 
////								blockpos, poseStack, pBuffer.getBuffer(renderType), false, new Random(), 
////								blockState.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY);
//					}
//				}
//				ForgeHooksClient.setRenderLayer(null);

				ResourceLocation texture = getTextureLocation(entity);
				if (texture != BLOCK_ATLAS) {
					for (RenderType blockRenderType : RenderType.chunkBufferLayers()) {
						RenderType renderType = null;
						if (blockRenderType == RenderType.solid()) {
							renderType = RenderType.entitySolid(texture);
						}
						else if (blockRenderType == RenderType.cutout() || blockRenderType == RenderType.cutoutMipped()) {
							renderType = RenderType.entityCutout(texture);
						}
						else if (blockRenderType == RenderType.translucent()) {
							renderType = RenderType.entityTranslucent(texture);
						}
						if (renderType != null) {
							VertexConsumer vertexBuilder = buffer.getBuffer(renderType);
							model.render(poseStack, vertexBuilder, packedLight, OverlayTexture.NO_OVERLAY);
						}
					}
				}

				poseStack.popPose();
			}
		}
	}

	public static final Set<Direction> ALL_VISIBLE = EnumSet.allOf(Direction.class);
	private ModelPart[] models = new ModelPart[64];
	private ModelPart[] modelsGlass = new ModelPart[64];
	private ModelPart getModelPart(BlockShardEntity entity) {
		long randomNum = entity.getUUID().getMostSignificantBits();
		int x = (int) (randomNum & 3);
		randomNum >>= 2;
		int y = (int) (randomNum & 3);
		randomNum >>= 2;
		int z = (int) (randomNum & 3);
		int index = x * 16 + y * 4 + z;

		ModelPart[] cache = entity.isGlass() ? modelsGlass : models;
		if (cache[index] == null) {
			int uv = (int) ((randomNum >> 27) & 255);
			int u = uv & 15;
			int v = (uv >> 4) & 15;
			float xSize = x + 5;
			float ySize = y + 5;
			float zSize = entity.isGlass() ? 1 : z + 5;
			ModelPart.Cube cube = new ModelPart.Cube(
					u, v,
					-xSize / 2, -ySize / 2, -zSize / 2, 
					xSize, ySize, zSize,
					0, 0, 0, false, 16, 16, ALL_VISIBLE);
			
			ModelPart model = new ModelPart(Collections.singletonList(cube), Collections.emptyMap());
			model.y = -3;
			cache[index] = model;
		}
		return cache[index];
	}

	private void rotate(BlockShardEntity entity, ModelPart modelRenderer, float partialTick) {
		long randomNum = entity.getUUID().getMostSignificantBits() >> 6;
		modelRenderer.xRot = (float) Math.PI * (int) (randomNum & 127) / 64;
		randomNum >>= 7;
		modelRenderer.yRot = (float) Math.PI * (int) (randomNum & 127) / 64;
		randomNum >>= 7;
		modelRenderer.zRot = (float) Math.PI * (int) (randomNum & 127) / 64;
	}

}
