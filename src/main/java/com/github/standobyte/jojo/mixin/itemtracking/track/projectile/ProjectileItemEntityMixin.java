package com.github.standobyte.jojo.mixin.itemtracking.track.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ThrowableItemProjectile.class)
public abstract class ProjectileItemEntityMixin extends ThrowableProjectile {

	protected ProjectileItemEntityMixin(EntityType<? extends ThrowableProjectile> p_i48540_1_, Level p_i48540_2_) {
		super(p_i48540_1_, p_i48540_2_);
	}
	
	@Shadow public abstract ItemStack getItem();

	@Inject(method = "setItem", at = @At("TAIL"))
	public void jojo_ripples$onSetItem(ItemStack oldItem, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide()) {
			ItemStack item = getItem();
			ItemTracker tracker = ItemTracking.getItemTracker(item, level);
			if (tracker != null) {
				tracker.setAtEntity(item, this.getId(), level, KnownItemState.ENTITY_IS_ITEM, null);
			}
		}
	}
}
