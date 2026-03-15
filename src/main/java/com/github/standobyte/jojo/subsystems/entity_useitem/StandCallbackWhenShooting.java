package com.github.standobyte.jojo.subsystems.entity_useitem;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class StandCallbackWhenShooting {

	/**
	 * @return The new projectile shooting vector, readjusted with the Stand's stats
	 */
	@Nullable
	public static Vec3 onStandShootingItemVanilla(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
		if (projectile.getOwner() instanceof StandEntity stand) {
			EntityActionInstance curAction = stand.getCurStandAction();
			if (curAction == null && ServerSideLivingClick.isEntityHoldingAnItem(stand)) {
				LivingComponentAction.getComponent(stand).setAction(new VanillaItemClickAsAction.ItemClickInstance(), SyncType.TRACKING_AND_SELF);
			}
			// if the stand is throwing multiple, for example, snowballs because the player is holding RMB, 
			// reset the 10 tick timer (recovery phase of VanillaItemClickAsAction.ItemClickInstance)
			else if (curAction.ability == ModSpecialActions.RMB_CLICK_ITEM.get()) {
				curAction.setStartingPhase();
				curAction.syncPhaseChanges();
			}
			
			curAction = stand.getCurStandAction();
			// if the stand used an item as a projectile, set the offset to the front of the user
			// and readjust the projectile position/movement before it's added
			if (curAction != null && 
					(curAction.ability == ModSpecialActions.RMB_CLICK_ITEM.get()
					|| curAction.ability == ModSpecialActions.RMB_USING_ITEM.get())) {
				velocity = StandStatFormulas.projectileVelocityScaling(stand.getAttackDamage(), velocity);
				inaccuracy = StandStatFormulas.projectileInaccuracyScaling(stand.getPrecision(), inaccuracy);
				Vec3 newProjectileVec = projectile.getMovementToShoot(x, y, z, velocity, inaccuracy);
				
				if (stand.offsetFromUser.isIdle()) {
					Vec3 standOffset = new Vec3(0, 0, 1.5);
					StandOffsetFromUser.Rotations rotations = StandOffsetFromUser.Rotations.HEAD_XY;
					stand.offsetFromUser.setOffset(standOffset, rotations);
					stand.offsetFromUser.syncToTracking();
	
					stand.updatePosition(stand.getUser());
					projectile.setPos(stand.getX(), stand.getEyeY() - 0.1, stand.getZ());
				}
				
				return newProjectileVec;
			}
		}
		return null;
	}

	public static <T extends Projectile> T shootProjectileWithStandStats(
			_Projectile.ProjectileFactory<T> factory,
			ServerLevel level,
			ItemStack spawnedFrom,
			LivingEntity owner,
			StandEntity ownerAsStand,
			float z,
			float velocity,
			float inaccuracy) {
		return _Projectile.spawnProjectile(
				factory.create(level, owner, spawnedFrom),
				level,
				spawnedFrom,
				projectile -> {
					final float _velocity;
					final float _inaccuracy;
					if (ownerAsStand != null) {
						_velocity = StandStatFormulas.projectileVelocityScaling(ownerAsStand.getAttackDamage(), velocity);
						_inaccuracy = StandStatFormulas.projectileInaccuracyScaling(ownerAsStand.getPrecision(), inaccuracy);
					}
					else {
						_velocity = velocity;
						_inaccuracy = inaccuracy;
					}
					projectile.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), z, _velocity, _inaccuracy);
				});
	}
	
}
