package com.github.standobyte.jojoimpl.stands.crazydiamond.client;

import java.util.List;

import com.github.standobyte.jojo.client.ui.marker.MarkerRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletAbility;

import net.minecraft.client.Minecraft;

public class CrazyDBloodHomingMarker extends MarkerRenderer {

	public CrazyDBloodHomingMarker(Minecraft mc) {
		super(JojoMod.resLoc("textures/icons/blood_drops.png"), mc);
		renderThroughBlocks = false;
		useStandSkinColor = true;
	}

	@Override
	protected boolean shouldRender() {
		// TODO hide the marker when the ability is not selected/on shift
		return true;
//		PrototypeAbilityHud hud = PowerHud.abilityHUDInstance;
//		ActionsOverlayGui hud = ActionsOverlayGui.getInstance();
//		return hud.showExtraActionHud(ModStandsInit.CRAZY_DIAMOND_BLOCK_BULLET.get())
//				&& !mc.player.isShiftKeyDown();
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		fillWithStandEffectTargets(list, partialTick, ModStandAbilities.EFFECT_CD_BLOOD_DROPS.get(), CrazyDBlockBulletAbility.PLAYER_TRACKING_RANGE, mc, true);
	}
}
