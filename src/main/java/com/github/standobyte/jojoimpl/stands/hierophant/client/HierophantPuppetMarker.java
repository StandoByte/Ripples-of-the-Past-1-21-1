package com.github.standobyte.jojoimpl.stands.hierophant.client;

import java.util.List;
import java.util.Optional;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.hud.marker.MarkerRenderer;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity.StandFlag;
import com.github.standobyte.jojoimpl.stands.hierophant.HierophantPuppetEffect;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class HierophantPuppetMarker extends MarkerRenderer {

	public HierophantPuppetMarker(Minecraft mc) {
		super("puppet", mc);
		renderThroughBlocks = true;
		useStandSkinColor = true;
	}

	@Override
	protected boolean shouldRender() {
		return true;
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		if (standPower == null) return;
		
		Optional<HierophantPuppetEffect> puppetingOptional = standPower.userStandEffects
				.getEffectOfType(ModStandAbilities.EFFECT_HG_PUPPET.get());
		if (!puppetingOptional.isPresent()) return;
		
		HierophantPuppetEffect puppeting = puppetingOptional.get();
		Entity puppetTarget = puppeting.getTarget();
		if (puppetTarget == null || !puppetTarget.isAlive()) return;
		
		StandEntity hierophant = standPower.getSummonedStandEntity();
		if (hierophant != null && hierophant.isAlive() && !hierophant.getStandFlag(StandFlag.MANUAL_CONTROL)) {
			list.add(new MarkerInstance(entityMarkerPos(puppetTarget, partialTick), false, puppetingOptional));
		}
	}

}
