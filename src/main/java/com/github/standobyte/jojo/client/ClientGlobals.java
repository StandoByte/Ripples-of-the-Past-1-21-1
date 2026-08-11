package com.github.standobyte.jojo.client;

import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;

import net.minecraft.client.Minecraft;

public class ClientGlobals {
	public static boolean canSeeStands;
	public static boolean canHearStands;
	public static SummonedStand playerStand;
	public static StandEntity playerStandEntity;
	public static double standPrecision;

	public static void tick(Minecraft mc) {
		if (mc.player != null) {
			StandPower stand = ClientPowerCache.getPower(PowerClass.STAND);
			playerStand = stand != null ? stand.getSummonedStand() : null;
			playerStandEntity = playerStand != null ? playerStand.getStandEntity() : null;
			canSeeStands = StandUtil.entityCanSeeStands(mc.player);
			canHearStands = canSeeStands;
		}
		else {
			playerStandEntity = null;
		}
		standPrecision = playerStandEntity != null ? playerStandEntity.getPrecision() : 0;
	}
}
