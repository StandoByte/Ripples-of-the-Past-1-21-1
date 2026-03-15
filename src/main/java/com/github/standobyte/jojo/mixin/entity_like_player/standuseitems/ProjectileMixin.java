package com.github.standobyte.jojo.mixin.entity_like_player.standuseitems;

import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.subsystems.entity_playerwrapper.EntityAsPlayerWrapper;
import com.github.standobyte.jojo.subsystems.entity_useitem.StandCallbackWhenShooting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity implements TraceableEntity {
	public ProjectileMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}
	
	
	@Shadow private UUID ownerUUID;

	@ModifyVariable(method = "setOwner", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	public Entity jojo_ripples$setActualOwner(@Nullable Entity owner) {
		if (owner instanceof EntityAsPlayerWrapper wrapper) {
			return wrapper.getEntity();
		}
		return owner;
	}

	@Inject(method = "ownedBy", at = @At("RETURN"), cancellable = true)
	public void jojo_ripples$standAndUserShareOwning(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (this.ownerUUID != null && !ci.getReturnValueZ()
				&& entity instanceof LivingEntity livingArg
				&& getOwner() instanceof LivingEntity livingOwner) {
			LivingEntity user1 = StandUtil.getStandUser(livingArg);
			LivingEntity user2 = StandUtil.getStandUser(livingOwner);
			if (user1 != null && user1 == user2) {
				ci.setReturnValue(true);
			}
		}
	}
	
	
	@ModifyVariable(method = "shoot", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
	public Vec3 jojo_ripples$onProjectileShot(Vec3 movementVec, double x, double y, double z, float velocity, float inaccuracy) {
		Vec3 newVec = StandCallbackWhenShooting.onStandShootingItemVanilla((Projectile) (Entity) this, x, y, z, velocity, inaccuracy);
		if (newVec != null) {
			return newVec;
		}
		return movementVec;
	}
}
