package com.github.standobyte.jojo.mixin.itemtracking.track.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowStuckOnHitMixin extends Projectile {

	public AbstractArrowStuckOnHitMixin(EntityType<? extends Projectile> type, Level level) {
		super(type, level);
	}

	@Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setArrowCount(I)V"))
	public void jojo_ripples$onArrowStuck(EntityHitResult hitResult, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide() && hitResult.getEntity() instanceof LivingEntity targetEntity) {
			ItemStack item = ((AbstractArrow) (Projectile) this).getPickupItemStackOrigin();
			ItemTracker tracker = ItemTracking.getItemTracker(item, level);
			if (tracker != null) {
				item = item.copy();
				tracker.setAtEntity(item, targetEntity.getId(), level, KnownItemState.STUCK_ARROW, trackerId -> 
						targetEntity.getArrowCount() > 0);
			}
		}
	}
}
