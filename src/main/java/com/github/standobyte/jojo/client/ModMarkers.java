package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.ui.marker.MarkerRenderer;
import com.github.standobyte.jojo.client.ui.marker.StandAimMarker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTrackDebugMarker;
import com.github.standobyte.jojo.subsystems.itemtracking.OriginalItemPosMarker;
import com.github.standobyte.jojo.tmp.charactertest.CloneCharactersMarker;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDBloodHomingMarker;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDOriginPosAnchorMarker;
import com.github.standobyte.jojoimpl.stands.hierophant.client.HierophantPuppetMarker;

import net.minecraft.client.Minecraft;

public class ModMarkers {

	public static void registerMarkers(Minecraft mc) {
		MarkerRenderer.registerMarkerRenderer(new StandAimMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new CloneCharactersMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new OriginalItemPosMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new CrazyDOriginPosAnchorMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new CrazyDBloodHomingMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new HierophantPuppetMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new ItemTrackDebugMarker(mc));
	}
}
