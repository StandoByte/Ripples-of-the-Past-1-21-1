package com.github.standobyte.jojoimpl.stands.crazydiamond.client;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityRenderer;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class CrazyDBlockBulletRenderer extends SimpleEntityRenderer<CrazyDBlockBulletEntity, EntityModel<CrazyDBlockBulletEntity>> {

	public CrazyDBlockBulletRenderer(EntityRendererProvider.Context renderManager) {
		super(renderManager);
	}

	@Override
	public ResourceLocation getTextureLocation(CrazyDBlockBulletEntity entity) {
		ResourceLocation texture = entity.getBlockTex();
		if (texture == null) {
			texture = getBlockTexture(entity);
			entity.setBlockTex(texture);
		}
		return texture;
	}

	private static final ResourceLocation GLASS_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/glass.png");
	public ResourceLocation getBlockTexture(CrazyDBlockBulletEntity entity) {
		if (entity.getBlock() != null) {
			ResourceLocation texture = getBlockTexture(entity.getBlock().defaultBlockState());
			return texture != null ? texture : GLASS_TEXTURE;
		}
		return GLASS_TEXTURE;
	}

	@Nullable
	public static ResourceLocation getBlockTexture(BlockState blockState) {
		BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
		List<BakedQuad> quads = blockModel.getQuads(blockState, Direction.NORTH, OOPMoment.RANDOM, ModelData.EMPTY, null);
		if (!quads.isEmpty()) {
			TextureAtlasSprite sprite = quads.get(0).getSprite();
			return getSpriteTexture(sprite);
		}
		return null;
	}

	@Nullable
	public static ResourceLocation getSpriteTexture(TextureAtlasSprite sprite) {
		if (sprite != null) {
			ResourceLocation name = sprite.contents().name();
			if (name != null) {
				return name.withPath(path -> "textures/" + path + ".png");
			}
		}
		return null;
	}
}
