package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.client.ModEntityTypeRenderers;
import com.github.standobyte.jojo.config.RotpConfig.ClothesArmorPriority;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesRSExtension;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

	@Inject(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;render("
			+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "I"
			+ "Lnet/minecraft/world/entity/LivingEntity;"
			+ "FFFFFF"
			+ ")V", at = @At("HEAD"), cancellable = true)
	private void cancelArmorRender(
			PoseStack poseStack,
			MultiBufferSource buffer,
			int packedLight,
			LivingEntity livingEntity,
			float limbSwing,
			float limbSwingAmount,
			float partialTicks,
			float ageInTicks,
			float netHeadYaw,
			float headPitch,
			CallbackInfo ci) {
		if (ModEntityTypeRenderers.cancelRenderArmor(livingEntity)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;"
			+ "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;"
			+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
			+ "Lnet/minecraft/world/entity/LivingEntity;"
			+ "Lnet/minecraft/world/entity/EquipmentSlot;"
			+ "I"
			+ "Lnet/minecraft/client/model/HumanoidModel;"
			+ "FFFFFF"
			+ ")V", at = @At("HEAD"), cancellable = true)
	private void cancelArmorPieceRender_Neo(
			PoseStack poseStack, 
			MultiBufferSource bufferSource, 
			LivingEntity livingEntity, 
			EquipmentSlot slot, 
			int packedLight, 
			HumanoidModel<?> armorModel, 
			float limbSwing, 
			float limbSwingAmount, 
			float partialTick, 
			float ageInTicks, 
			float netHeadYaw, 
			float headPitch,
			CallbackInfo ci) {
		if (JojoMod.config.getCommon().clothesArmorPriority.get() == ClothesArmorPriority.CLOTHES) {
			HumanoidClothesRSExtension entityClothes = HumanoidClothesRSExtension.getCurRenderData();
			if (entityClothes != null) {
				ClothesSlotType clothesSlot = switch (slot) {
					case HEAD -> ClothesSlotType.HEAD;
					case CHEST -> ClothesSlotType.CHEST;
					case LEGS -> ClothesSlotType.LEGS;
					case FEET -> ClothesSlotType.FEET;
					default -> null;
				};
				if (clothesSlot != null && !entityClothes.items.get(clothesSlot).isEmpty()) {
					ci.cancel();
				}
			}
		}
	}

}
