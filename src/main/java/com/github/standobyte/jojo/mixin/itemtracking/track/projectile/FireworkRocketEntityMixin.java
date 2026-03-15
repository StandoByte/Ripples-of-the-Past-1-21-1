package com.github.standobyte.jojo.mixin.itemtracking.track.projectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends Projectile {

	public FireworkRocketEntityMixin(EntityType<? extends Projectile> p_i231584_1_, Level p_i231584_2_) {
		super(p_i231584_1_, p_i231584_2_);
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At("RETURN"))
	public void jojo_ripples$onEntityCreated(Level level, double x, double y, double z, ItemStack item, CallbackInfo ci) {
		ItemTracker tracker = ItemTracking.getItemTracker(item, level);
		if (tracker != null) {
			tracker.setAtEntity(item.copy(), this.getId(), level, KnownItemState.ENTITY_IS_ITEM, null);
		}
	}

}
