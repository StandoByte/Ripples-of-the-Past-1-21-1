package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.client.ui.hud.marker.StandAimMarker;
import com.github.standobyte.jojo.tmp.charactertest.CloneCharactersMarker;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDBloodHomingMarker;
import com.github.standobyte.jojoimpl.stands.hierophant.client.HierophantPuppetMarker;

import net.minecraft.client.Minecraft;

public class InitMarkers {

	public static void registerMarkers(Minecraft mc) {
		MarkerRenderer.registerMarkerRenderer(new StandAimMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new CloneCharactersMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new CrazyDBloodHomingMarker(mc));
		MarkerRenderer.registerMarkerRenderer(new HierophantPuppetMarker(mc));
	}
}
