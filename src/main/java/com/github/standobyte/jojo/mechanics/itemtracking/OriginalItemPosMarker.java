package com.github.standobyte.jojo.mechanics.itemtracking;

import java.util.List;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTrackDebugMarker.ItemMarkerInstance;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class OriginalItemPosMarker extends MarkerRenderer {

	public OriginalItemPosMarker(Minecraft mc) {
		super((String) null, mc);
		renderThroughBlocks = true;
	}

	@Override
	protected boolean shouldRender() {
		return true;
	}

	@Override
	protected void renderIcon(PoseStack poseStack, MarkerInstance marker, float partialTick, StandSkin standSkin) {
		ItemStack item = ((ItemMarkerInstance) marker).item;
		if (item != null && !item.isEmpty()) {
			renderItem(poseStack, item, partialTick);
		}
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		if (mc.player != null) {
			for (InteractionHand hand : InteractionHand.values()) {
				checkAddItemMarker(list, mc.player.getItemInHand(hand));
			}
		}
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			for (InteractionHand hand : InteractionHand.values()) {
				checkAddItemMarker(list, stand.getItemInHand(hand));
			}
		}
	}
	
	protected void checkAddItemMarker(List<MarkerInstance> markers, ItemStack item) {
		if (!item.isEmpty()) {
			OriginalItemPosComponent originalPos = item.get(ModItemDataComponents.ORIGINAL_POS);
			if (originalPos != null) {
				BlockPos blockPos = originalPos.blockPos();
				boolean outline = false;
				markers.add(new ItemMarkerInstance(Vec3.upFromBottomCenterOf(blockPos, 1.0), outline, item));
			}
		}
	}

}
