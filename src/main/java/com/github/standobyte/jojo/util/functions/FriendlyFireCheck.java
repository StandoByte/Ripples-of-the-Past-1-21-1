package com.github.standobyte.jojo.util.functions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

// I can't think of another way to write this that doesn't make my head explode
public class FriendlyFireCheck {
	
	public static class EntityCast {
		public Entity entity;
		@Nullable public TraceableEntity asProjectile;
		@Nullable public Entity projectileOwner;
		@Nullable public StandEntity asStand;
		public LivingEntity playerOrMobResponsible;
		@Nullable public LivingEntity petOwner;
		
		public void fill(Entity entity) {
			this.entity = entity;
			asProjectile = entity instanceof TraceableEntity projectile ? projectile : null;
			projectileOwner = asProjectile != null ? asProjectile.getOwner() : null;
			asStand = 
					entity instanceof StandEntity stand ? stand : 
					projectileOwner instanceof StandEntity stand ? stand : null;
			playerOrMobResponsible = 
					asStand != null ? asStand.getUser() : 
					projectileOwner instanceof LivingEntity living ? living :
					entity instanceof LivingEntity living ? living : null;
			petOwner = playerOrMobResponsible instanceof OwnableEntity tameable ? tameable.getOwner() : null;
		}
		
		public void clear() {
			entity = null;
			asProjectile = null;
			projectileOwner = null;
			asStand = null;
			playerOrMobResponsible = null;
			petOwner = null;
		}
	}
	
	public static boolean isAllowedToHurt(Entity damagingEntity, Entity targetEntity) {
		return isAllowedToHurt(damagingEntity, targetEntity, false);
	}
	
	public static boolean isAllowedToHurt(Entity damagingEntity, Entity targetEntity, boolean projectileCanHurtOwner) {
		EntityCast attacker = new EntityCast();
		EntityCast target = new EntityCast();
		attacker.fill(damagingEntity);
		target.fill(targetEntity);
		boolean result = isAllowedToHurt(attacker, target, projectileCanHurtOwner);
		return result;
	}
	
	public static boolean isAllowedToHurt(EntityCast attacker, EntityCast target, boolean projectileCanHurtOwner) {
		if (attacker.playerOrMobResponsible == null || target.playerOrMobResponsible == null) {
			return true;
		}
		
		PlayerTeam team1 = attacker.playerOrMobResponsible.getTeam();
		PlayerTeam team2 = target.playerOrMobResponsible.getTeam();
		if (team1 != null && team1.isAlliedTo(team2) && !team1.isAllowFriendlyFire()) {
			return false;
		}
		
		if (attacker.playerOrMobResponsible == target.playerOrMobResponsible) {
			if (attacker.entity == attacker.asProjectile) {
				return projectileCanHurtOwner;
			}
			if (attacker.entity == attacker.asStand) {
				return attacker.asStand.canHitUser();
			}
			if (target.entity == target.asStand) {
				return false;
			}
		}
		else {
			if (attacker.playerOrMobResponsible instanceof ServerPlayer serverPlayer
					&& target.playerOrMobResponsible instanceof Player
					&& !serverPlayer.getServer().isPvpAllowed()) {
				return false;
			}
			
			if (attacker.petOwner == target.playerOrMobResponsible || attacker.playerOrMobResponsible == target.petOwner) {
				return false;
			}
		}
		
		return true;
	}
}
