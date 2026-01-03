package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.client.ui.hud.marker.StandAimMarker;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.client.CrazyDBloodHomingMarker;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.client.CrazyDOriginPosAnchorMarker;
import com.github.standobyte.jojo.jojoimpl.stands.hierophant.client.HierophantPuppetMarker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTrackDebugMarker;
import com.github.standobyte.jojo.mechanics.itemtracking.OriginalItemPosMarker;
import com.github.standobyte.jojo.tmp.charactertest.CloneCharactersMarker;

import net.minecraft.client.Minecraft;

public class InitMarkers {

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
