package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;

import net.minecraft.world.phys.Vec3;

public class ClientStandEntityStuff {
	public final BarrageSwings barrageSwings = new BarrageSwings();

	public List<Vec3> tiltVecQueue = new ArrayList<>();
	public float lastMotionTiltTick = -1;
	
	public boolean summonAnimStopped = false;
}
