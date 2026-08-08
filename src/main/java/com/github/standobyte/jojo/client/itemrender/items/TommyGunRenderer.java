package com.github.standobyte.jojo.client.itemrender.items;

import com.github.standobyte.jojo.client.itemrender.custommodel.CustomItemRenderer;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.sidecontent.item.tommygun.TommyGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TommyGunRenderer extends CustomItemRenderer {
	private final ResourceLocation[] fireTexture = new ResourceLocation[] {
			JojoMod.resLoc("textures/item/tommy_gun_fire_1.png"),
			JojoMod.resLoc("textures/item/tommy_gun_fire_2.png")
	};

	public TommyGunRenderer(Minecraft mc, ResourceLocation rotpGeckoModelPath, ResourceLocation modelTexture) {
		super(mc, rotpGeckoModelPath, modelTexture);
	}

	@Override
	public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		switch (displayContext) {
			case GUI:
			case GROUND:
			case FIXED:
				BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
				renderItemNormally(poseStack, itemStack, displayContext, renderTypeBuffer, light, overlay, model);
				break;
			default:
				super.renderByItem(itemStack, displayContext, poseStack, renderTypeBuffer, light, overlay);
				break;
		}
	}

	@Override
	protected void doRender(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		ModelPart root = model.getModelRoot();
		if (root != null) {
			ModelPart gun = root.children.get("tommyGun");
			if (gun != null) {
				VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
						renderTypeBuffer, renderType(texture), false, itemStack.hasFoil());
				gun.render(poseStack, vertexBuilder, light, overlay, 0xFFFFFFFF);
			}
			
			ModelPart fire = root.children.get("fire");
			if (fire != null) {
				float fireRatio = TommyGunItem.getGunshotTick(itemStack) - ClientUtil.partialTick();
				if (fireRatio > 1f) {
					ResourceLocation texture = fireTexture[fireRatio >= 1.5f ? 1 : 0];
					VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
							renderTypeBuffer, renderType(texture), false, itemStack.hasFoil());
					fire.render(poseStack, vertexBuilder, ClientUtil.MAX_LIGHT, overlay, 0xFFFFFFFF);
				}
			}
		}
	}

}
