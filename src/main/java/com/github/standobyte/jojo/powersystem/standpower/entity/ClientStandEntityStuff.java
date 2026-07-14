package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithId;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.util.objects_java.Lerp;

import net.minecraft.world.phys.Vec3;

public class ClientStandEntityStuff {
	public final BarrageSwings barrageSwings = new BarrageSwings();

	public List<Vec3> tiltVecQueue = new ArrayList<>();
	public float lastMotionTiltTick = -1;
	
	public boolean summonAnimStopped = false;
	public AnimWithId specificSummonAnim = null;
	public float freezeAtSummonPoseTime = 0;
	
    public Lerp.FloatValue modelAlpha = new Lerp.FloatValue(1);
    
    public void tick() {
		modelAlpha.set(1, true);
    }
    
    public float getAlpha(StandEntity standEntity, float partialTick) {
    	return (float) standEntity.rangeEfficiency * modelAlpha.lerp(partialTick);
    }
    
}
