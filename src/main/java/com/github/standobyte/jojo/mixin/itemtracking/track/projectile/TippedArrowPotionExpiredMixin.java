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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(AbstractArrow.class)
public abstract class TippedArrowPotionExpiredMixin extends Projectile {

	protected TippedArrowPotionExpiredMixin(EntityType<? extends AbstractArrow> type, Level world) {
		super(type, world);
	}
	
	@Shadow private ItemStack pickupItemStack;

	@Inject(method = "setPickupItemStack", at = @At("HEAD"))
	public void jojo_ripples$onSetArrowItem(ItemStack newItem, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(this.pickupItemStack, level);
			if (tracker != null) {
				tracker.setAtEntity(newItem, this.getId(), level, KnownItemState.ENTITY_IS_ITEM, null);
			}
		}
	}

}
