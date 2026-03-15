package com.github.standobyte.jojo.init;

import java.util.HashMap;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.explosion.CustomExplosion.CustomExplosionSupplier;
import com.github.standobyte.jojoimpl.stands._entitybase.StandEntityHeavyPunchAbility;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;

public class ModCustomExplosions {

	public static final ResourceLocation CROSSFIRE_HURRICANE = JojoMod.resLoc("cfh");
	public static final ResourceLocation PILLAR_MAN_DETONATION = JojoMod.resLoc("acdc");
	public static final ResourceLocation HAMON = JojoMod.resLoc("hamon");
	public static final ResourceLocation STAND_HEAVY_PUNCH = JojoMod.resLoc("heavy_punch");

	public static final HashMap<ResourceLocation, CustomExplosionSupplier> REGISTER = Util.make(new HashMap<>(), map -> {
		// map.put(CROSSFIRE_HURRICANE, MRCrossfireHurricaneEntity.CrossfireHurricaneExplosion::new);
		// map.put(PILLAR_MAN_DETONATION, PillarmanSelfDetonation.PillarmanExplosion::new);
		// map.put(HAMON, HamonBlastExplosion::new);
		map.put(STAND_HEAVY_PUNCH, StandEntityHeavyPunchAbility.HeavyPunchExplosion::new);
	});
}
