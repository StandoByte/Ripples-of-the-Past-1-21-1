package com.github.standobyte.jojo.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.init.ModMapDecorations;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

@Mixin(targets = "net/minecraft/client/gui/MapRenderer$MapInstance")
public class LargeMapIcons {
	
	@Inject(
			method = "Lnet/minecraft/client/gui/MapRenderer$MapInstance;draw("
					+ "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "Lnet/minecraft/client/renderer/MultiBufferSource;"
					+ "ZI)V",
			at = @At(value = "INVOKE", 
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;"
					+ "last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"),
			slice = @Slice(
					from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getDecorations()Ljava/lang/Iterable;"),
					to = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/MapDecorationTextureManager;get(Lnet/minecraft/world/level/saveddata/maps/MapDecoration;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;")))
	public void scaleSprite(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, 
			CallbackInfo ci, @Local MapDecoration mapDecoration) {
		if (ModMapDecorations.LARGE_ICONS.contains(mapDecoration.type())) {
			poseStack.scale(2, 2, 1);
		}
	}
}
