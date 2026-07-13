package com.github.standobyte.jojo.client.entityanim.player;

import com.github.standobyte.jojo.client.entityanim.player.HumanoidModelPartsWithBends.AlternativeModelPart;
import com.mojang.blaze3d.vertex.PoseStack;

public class TmpBendCrutches {

	public static void partiallyFixTorsoClothesParts(AlternativeModelPart modelPart, PoseStack poseStack) {
		boolean isTorsoPart = !modelPart.name().contains("head");
		if (isTorsoPart) {
			poseStack.translate(0, -0.375, 0);
		}
	}
	
}
