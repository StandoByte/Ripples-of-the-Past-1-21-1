package com.github.standobyte.jojo.mechanics.clothes.client.layer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.UglyCrutchesClient;
import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.firstperson.FirstPersonModelLayer;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.v1_21_4_stuff.renderstate.ExtractRSExtensionManually;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

//public class HumanoidClothesLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class HumanoidClothesLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> implements FirstPersonModelLayer {
	private static final ClothesSlotType[] RENDER_ORDER = {
			ClothesSlotType.CHEST,
			ClothesSlotType.HEAD,
			ClothesSlotType.LEGS,
			ClothesSlotType.FEET
	};
	
	public HumanoidClothesLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, 
			int packedLight, T livingEntity,
			float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, 
			float netHeadYaw, float headPitch) {
//	public void render(PoseStack poseStack, MultiBufferSource bufferSource, 
//			int packedLight, S renderState, float yRot, float xRot) {
//		HumanoidClothesRSExtension clothesRS = renderState.getRenderData(ModEntityRenderers.CLOTHES_CONTEXT);
		if (livingEntity.isInvisible()) return;
		
		M parentModel = getParentModel();
		int overlay = LivingEntityRenderer.getOverlayCoords(livingEntity, 0);
		render(parentModel, 
				livingEntity, ClientUtil.partialTick(livingEntity), 
				poseStack, bufferSource, packedLight, overlay);
	}
	
	/**
	 * Don't forget to fill {@link HumanoidClothesRSExtension} before rendering this from outside.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity, M extends HumanoidModel<T>> void render(M parentModel, 
			@Nullable LivingEntity entity, float partialTick, 
			PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int overlay) {
		HumanoidClothesRSExtension clothesRS = HumanoidClothesRSExtension.getCurRenderData();
		if (clothesRS == null) return;
		ClothesModelLoader clothesModels = ClothesModelLoader.getInstance();
		if (clothesModels == null) return;
		for (ClothesSlotType piece : RENDER_ORDER) {
			ItemStack clothesItem = clothesRS.items.get(piece); if (clothesItem.isEmpty()) continue;
			var clothesComponent = clothesItem.get(ModItemDataComponents.CLOTHES_PIECE.get()); if (clothesComponent == null) continue;
			var clothesPiece = clothesComponent.getPiece(); if (clothesPiece == null) continue;

			ResourceLocation texturePath = clothesPiece.textureActualPath;
			ResourceLocation modelPath = clothesPiece.modelId.location();
			ClothesModelEntry modelEntry = clothesModels.getClothesModelEntry(modelPath); if (modelEntry == null) continue;
			
			HumanoidClothesModel clothesModel = modelEntry.getModel();
			parentModel.copyPropertiesTo((M) clothesModel);
			clothesModel.setClothesPartsVisibility(clothesRS.slimModel, piece);
			clothesModel.poseClothes(parentModel);
			UglyCrutchesClient.adjustClothesWithVanillaAnim(clothesModel, entity, partialTick);
			VertexConsumer ivertexbuilder = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texturePath));
			clothesModel.renderToBuffer(poseStack, ivertexbuilder, packedLight, overlay);
		}
	}
	
	
	@SubscribeEvent
	public static void beforeEntityRender(RenderLivingEvent.Pre<?, ?> event) {
		beforeEntityRender(event.getEntity(), event.getRenderer().getModel());
	}
	
	public static void beforeEntityRender(LivingEntity entity, EntityModel<?> model) {
		ExtractRSExtensionManually.extractClothes(entity);
		if (model instanceof PlayerModel playerModel) {
			disablePlayerOuterLayer(playerModel, HumanoidClothesRSExtension.getCurRenderData());
		}
	}
	
	public static void disablePlayerOuterLayer(PlayerModel<?> playerModel, HumanoidClothesRSExtension clothesRS) {
		if (clothesRS != null) {
			if (!clothesRS.items.get(ClothesSlotType.HEAD).isEmpty()) {
				playerModel.hat.visible = false;
			}
			if (!clothesRS.items.get(ClothesSlotType.CHEST).isEmpty()) {
				playerModel.jacket.visible = false;
				playerModel.leftSleeve.visible = false;
				playerModel.rightSleeve.visible = false;
			}
			if (!clothesRS.items.get(ClothesSlotType.LEGS).isEmpty()) {
				playerModel.leftPants.visible = false;
				playerModel.rightPants.visible = false;
			}
		}
	}

	@SubscribeEvent
	public static void clear(RenderLivingEvent.Post<?, ?> event) {
		ExtractRSExtensionManually.resetClothes();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void renderHandFirstPerson(HumanoidArm side, PoseStack poseStack, MultiBufferSource buffer, int light,
			LivingEntity entity, LivingEntityRenderer<?, ?> entityRenderer) {
		if (entity.isInvisible()) return;
		
		ExtractRSExtensionManually.extractClothes(entity);
		HumanoidClothesRSExtension clothes = HumanoidClothesRSExtension.getCurRenderData();
		if (clothes == null) return;
		ClothesModelLoader clothesModels = ClothesModelLoader.getInstance();
		if (clothesModels == null) return;
		
		M parentModel = getParentModel();
		for (ClothesSlotType piece : RENDER_ORDER) {
			ItemStack clothesItem = clothes.items.get(piece); if (clothesItem.isEmpty()) continue;
			var clothesComponent = clothesItem.get(ModItemDataComponents.CLOTHES_PIECE.get()); if (clothesComponent == null) continue;
			var clothesPiece = clothesComponent.getPiece(); if (clothesPiece == null) continue;
			
			ResourceLocation texturePath = clothesPiece.textureActualPath;
			ResourceLocation modelPath = clothesPiece.modelId.location();
			ClothesModelEntry modelEntry = clothesModels.getClothesModelEntry(modelPath); if (modelEntry == null) continue;
			
			HumanoidClothesModel clothesModel = modelEntry.getModel();
			parentModel.copyPropertiesTo((M) clothesModel);
			clothesModel.setClothesPartsVisibility(clothes.slimModel, piece);
			clothesModel.poseClothes(parentModel);
			UglyCrutchesClient.adjustClothesWithVanillaAnim(clothesModel, entity, ClientUtil.partialTick(entity));
			VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityCutoutNoCull(texturePath));
			
			clothesModel.head.visible = false;
			clothesModel.body.visible = false;
			clothesModel.rightLeg.visible = false;
			clothesModel.leftLeg.visible = false;
			clothesModel.hat.visible = false;
			
			ModelPart arm = FirstPersonModelLayer.getArm(clothesModel, side);
			arm.xRot = 0.0F;
			arm.render(poseStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
			ModelPart armSlim = side == HumanoidArm.LEFT ? clothesModel.leftArmSlim : clothesModel.rightArmSlim;
			armSlim.xRot = 0.0F;
			armSlim.render(poseStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
		}
		ExtractRSExtensionManually.resetClothes();
	}
	
	@Override
	public void renderFirstPersonAnimated(AnimFramePose pose, PoseStack poseStack, MultiBufferSource buffer, int light,
			LivingEntity entity, LivingEntityRenderer<?, ?> entityRenderer) {
		if (entity.isInvisible()) return;
		
		ExtractRSExtensionManually.extractClothes(entity);
		HumanoidClothesRSExtension clothes = HumanoidClothesRSExtension.getCurRenderData();
		if (clothes == null) return;
		ClothesModelLoader clothesModels = ClothesModelLoader.getInstance();
		if (clothesModels == null) return;
		
		M parentModel = getParentModel();
		for (ClothesSlotType piece : RENDER_ORDER) {
			ItemStack clothesItem = clothes.items.get(piece); if (clothesItem.isEmpty()) continue;
			var clothesComponent = clothesItem.get(ModItemDataComponents.CLOTHES_PIECE.get()); if (clothesComponent == null) continue;
			var clothesPiece = clothesComponent.getPiece(); if (clothesPiece == null) continue;
			
			ResourceLocation texturePath = clothesPiece.textureActualPath;
			ResourceLocation modelPath = clothesPiece.modelId.location();
			ClothesModelEntry modelEntry = clothesModels.getClothesModelEntry(modelPath); if (modelEntry == null) continue;
			
			HumanoidClothesModel clothesModel = modelEntry.getModel();
			parentModel.copyPropertiesTo((M) clothesModel);
			clothesModel.setClothesPartsVisibility(clothes.slimModel, piece);
			clothesModel.poseClothes(parentModel);
			UglyCrutchesClient.adjustClothesWithVanillaAnim(clothesModel, entity, ClientUtil.partialTick(entity));
			VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityCutoutNoCull(texturePath));
			
			clothesModel.head.visible = false;
			clothesModel.body.visible = false;
			clothesModel.rightLeg.visible = false;
			clothesModel.leftLeg.visible = false;
			clothesModel.hat.visible = false;

			((IHumanoidAnimModel) clothesModel).jojo_ripples$setupHumanoidPose(pose);
			clothesModel.renderToBuffer(poseStack, vertexBuilder, light, OverlayTexture.NO_OVERLAY);
		}
		ExtractRSExtensionManually.resetClothes();
	}

}