package com.github.standobyte.jojo.subsystems.itemtracking;

import java.util.List;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.ui.marker.MarkerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ItemTrackDebugMarker extends MarkerRenderer {

	public ItemTrackDebugMarker(Minecraft mc) {
		super((String) null, mc);
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
		ItemTracking allTrackers = ItemTracking.getItemTracking(mc.level);
		for (ItemTracker tracker : allTrackers.values()) {
			if ("debug".equals(tracker.context)) {
				Vec3 pos = null;
				
				Entity entity = tracker.getAtEntity(mc.level);
				if (entity != null) {
					pos = entityMarkerPos(entity, partialTick);
				}
				
				if (pos == null) {
					BlockPos blockPos = tracker.getAtBlockPos();
					if (blockPos != null) {
						pos = blockMarkerPos(blockPos);
					}
				}

				if (pos != null) {
					list.add(new ItemMarkerInstance(pos, false, tracker.itemStack));
				}
			}
		}
	}


	public static class ItemMarkerInstance extends MarkerInstance {
		final ItemStack item;

		public ItemMarkerInstance(Vec3 pos, boolean outlined, ItemStack itemStack) {
			super(pos, outlined);
			this.item = itemStack;
		}
	}
}
