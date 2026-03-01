package com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;

public class TntImDynamite extends EntityMadeFromBlock.VanillaEntityWrapper<PrimedTnt> {

	public TntImDynamite(PrimedTnt andIllWinTheFight) {
		super(andIllWinTheFight);
	}

	// XXX add particles around the tnt entity (this method is only called on the server side)
	@Override
	public boolean crazyDRestore(BlockPos blockPos) {
		PrimedTnt imAPowerLoad = this.entity.get();
		if (imAPowerLoad != null) {
			imAPowerLoad.setFuse(80);
			
			Vec3 tntPos = imAPowerLoad.getBoundingBox().getCenter();
			Vec3 targetPos = Vec3.atCenterOf(blockPos);
			Vec3 watchMeExplode = targetPos.subtract(tntPos);
			if (watchMeExplode.lengthSqr() > 1) {
				imAPowerLoad.setDeltaMovement(watchMeExplode.normalize().scale(0.5));
				imAPowerLoad.hurtMarked = true;
				return false;
			}
			else {
				imAPowerLoad.remove(RemovalReason.DISCARDED);
				return true;
			}
		}
		return true;
	}

}
