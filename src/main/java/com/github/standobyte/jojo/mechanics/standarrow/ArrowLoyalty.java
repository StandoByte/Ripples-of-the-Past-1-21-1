package com.github.standobyte.jojo.mechanics.standarrow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

public class ArrowLoyalty {
	private boolean dealtDamage;

    public static byte getLoyaltyFromItem(Entity entity, ItemStack stack) {
    	return entity.level() instanceof ServerLevel serverlevel
    			? (byte)Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverlevel, stack, entity), 0, 127)
				: 0;
    }

	public void tickPre(AbstractArrow arrow, int loyalty, int inGroundTime) {
		if (inGroundTime > 4) {
			this.dealtDamage = true;
		}

		Entity owner = arrow.getOwner();
		if (loyalty > 0 && (this.dealtDamage || arrow.isNoPhysics()) && owner != null) {
			if (!owner.isAlive() || owner.isSpectator()) {
				if (!arrow.level().isClientSide && arrow.pickup == AbstractArrow.Pickup.ALLOWED) {
					arrow.spawnAtLocation(arrow.getPickupItem(), 0.1F);
				}

				arrow.discard();
			} else {
				arrow.setNoPhysics(true);
				Vec3 posDiffToOwner = owner.getEyePosition().subtract(arrow.position());
				arrow.setPosRaw(
						arrow.getX(), 
						arrow.getY() + posDiffToOwner.y * 0.015 * (double)loyalty, 
						arrow.getZ());
				if (arrow.level().isClientSide) {
					arrow.yOld = arrow.getY();
				}

				arrow.setDeltaMovement(arrow.getDeltaMovement()
						.scale(0.95)
						.add(posDiffToOwner.normalize().scale(0.05 * (double)loyalty)));
			}
		}
	}
	
	public boolean canHitEntity() {
		return !dealtDamage;
	}
	
	public boolean canPlayerPickUp(AbstractArrow arrow, Player player, int loyalty) {
		return loyalty == 0 || arrow.ownedBy(player) || arrow.getOwner() == null;
	}
	
	public boolean tickDespawn(AbstractArrow arrow, int loyalty) {
		return arrow.pickup != AbstractArrow.Pickup.ALLOWED || loyalty <= 0;
	}





	public void readAdditionalSaveData(CompoundTag compound) {
		this.dealtDamage = compound.getBoolean("DealtDamage");
	}

	public void addAdditionalSaveData(CompoundTag compound) {
		compound.putBoolean("DealtDamage", this.dealtDamage);
	}
}
