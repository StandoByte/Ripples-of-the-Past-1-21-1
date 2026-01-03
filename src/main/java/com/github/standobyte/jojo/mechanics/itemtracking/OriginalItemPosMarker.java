package com.github.standobyte.jojo.mechanics.itemtracking;

import java.util.List;
import java.util.function.BiConsumer;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTrackDebugMarker.ItemMarkerInstance;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
		iterateHeldItemsOriginalPos((BlockPos blockPos, ItemStack item) -> list.add(new ItemMarkerInstance(blockMarkerPos(blockPos), false, item)));
	}
	
	public static void iterateHeldItemsOriginalPos(BiConsumer<BlockPos, ItemStack> action) {
		 forEntityHeldItem(action, Minecraft.getInstance().player);
		 forEntityHeldItem(action, ClientGlobals.playerStandEntity);
	}
	
	public static void forEntityHeldItem(BiConsumer<BlockPos, ItemStack> action, LivingEntity entity) {
		if (entity != null) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack item = entity.getItemInHand(hand);
				if (!item.isEmpty()) {
					OriginalItemPosComponent originalPos = item.get(ModItemDataComponents.ORIGINAL_POS);
					if (originalPos != null) {
						BlockPos blockPos = originalPos.blockPos();
						action.accept(blockPos, item);
					}
				}
			}
		}
	}

}
