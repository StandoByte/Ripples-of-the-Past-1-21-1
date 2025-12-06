package com.github.standobyte.jojo.mechanics.itemtracking;

import java.util.List;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.mc.item.DebugItem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class _ItemTrackDebugMarker extends MarkerRenderer {

	public _ItemTrackDebugMarker(Minecraft mc) {
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
		ItemTracker tracker = ItemTracking.getItemTracker(DebugItem.trackerId, mc.level);
		if (tracker != null && tracker.itemStack != null) {
			Vec3 pos = tracker.markerPos(mc.level, partialTick);
			if (pos != null) {
				list.add(new ItemMarkerInstance(pos, false, tracker.itemStack));
			}
		}
	}


	private static class ItemMarkerInstance extends MarkerInstance {
		final ItemStack item;

		public ItemMarkerInstance(Vec3 pos, boolean outlined, ItemStack itemStack) {
			super(pos, outlined);
			this.item = itemStack;
		}
	}
}
