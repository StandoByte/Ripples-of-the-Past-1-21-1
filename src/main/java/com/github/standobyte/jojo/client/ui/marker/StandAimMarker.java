package com.github.standobyte.jojo.client.ui.marker;

import java.util.List;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.ClientsideAim;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.client.ClientModSettings;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class StandAimMarker extends MarkerRenderer {
	public static final GuiIcon icon = new GuiIcon(JojoMod.resLoc("textures/hud/stand_aim_marker.png"), 31, 31);
	
	public StandAimMarker(Minecraft mc) {
		super(icon, null, mc);
	}

	@Override
	protected boolean shouldRender() {
		if (!ClientModSettings.getSettingsReadOnly().standAimMarker || mc.player == null) return false;
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		return standPower != null && standPower.getSummonedStandEntity() != null;
	}

	@Override
	protected void updatePositions(List<MarkerInstance> list, float partialTick) {
		ActionTarget target = ClientsideAim.standAim.getTarget();
		switch (target.getType()) {
			case BLOCK -> list.add(new MarkerInstance(Vec3.atCenterOf(target.getBlockPos())));
			case ENTITY -> {
				Entity entity = target.getEntity();
				list.add(new MarkerInstance(entity.getPosition(partialTick).add(0, entity.getBbHeight() / 2, 0)));
			}
			default -> {}
		}
	}

	@Override
	protected void renderAt(PoseStack poseStack, MarkerInstance marker, Camera camera, 
			Vec3 diff, float partialTick, StandSkin standSkin, int color) {
		poseStack.pushPose();

		double distance = diff.length();
		float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);

		poseStack.translate(diff.x, diff.y, diff.z);
		poseStack.scale(-scale * 1.25f, -scale * 1.25f, 1);

		icon.render(poseStack, -icon.width / 2, -icon.height / 2);

		poseStack.popPose();
	}

}
