package com.github.standobyte.jojo.client.itemrender.custommodel;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BakedCustomModel implements BakedModel {
	private BakedModel existingModel;
	private ISTERItemCaptureEntity captureEntityOverrides = null;

	public BakedCustomModel(BakedModel existingModel) {
		this.existingModel = existingModel;
	}

	public BakedCustomModel setCaptureEntity() {
		captureEntityOverrides = new ISTERItemCaptureEntity();
		return this;
	}

	// я хуй его знает, как всегда нахуевертили, лишь бы что-нибудь да переписать, а мне ебись и вспоминай, как все это у них там работает
//	public ItemISTERModelWrapper refreshOverrides(Map<ModelResourceLocation, BakedModel> registry) {
//		ItemOverrides overridesList = existingModel.getOverrides();
//		if (overridesList != null) {
//			List<ItemOverride> overrides = ClientReflection.getOverrides(overridesList);
//			if (!overrides.isEmpty()) {
//				List<BakedModel> overrideModels = ClientReflection.getOverrideModels(overridesList);
//				for (int i = 0; i < overrides.size(); i++) {
//					ItemOverride override = overrides.get(i);
//					ResourceLocation key = override.getModel();
//					ModelResourceLocation modelKey = ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(
//							key.getNamespace(), key.getPath().replace("item/", "")));
//					BakedModel replacementModel = registry.get(modelKey);
//					if (replacementModel != null) {
//						overrideModels.set(i, replacementModel);
//					}
//				}
//			}
//		}
//		return this;
//	}

	@SuppressWarnings("deprecation")
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, @Nonnull RandomSource rand) {
		return this.existingModel.getQuads(state, direction, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return this.existingModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return this.existingModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.existingModel.getParticleIcon();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemTransforms getTransforms() {
		return this.existingModel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		if (captureEntityOverrides != null) {
			return captureEntityOverrides;
		}
		return this.existingModel.getOverrides();
	}

}
