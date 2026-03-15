package com.github.standobyte.jojo.mixin.itemtracking.rigged;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

@Mixin(Mob.class)
public abstract class MobEntityLootingMixin extends LivingEntity {

	protected MobEntityLootingMixin(EntityType<? extends LivingEntity> p_i48577_1_, Level p_i48577_2_) {
		super(p_i48577_1_, p_i48577_2_);
	}

	@Shadow protected abstract void pickUpItem(ItemEntity itemEntity);
	@Shadow public abstract boolean wantsToPickUp(ItemStack stack);
	@Shadow public abstract boolean canPickUpLoot();

	@Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
	public void jojo_ripples$looting2(CallbackInfo ci) {
		// in this case this won't work anyway, so no need to run this extra code
		if (this.getType() == ModEntityTypes.CHARACTER.get()) return;

		Entity thisEntity = (Entity) this;
		if (this.canPickUpLoot() || thisEntity instanceof AbstractSkeleton || thisEntity instanceof Zombie) {
			Level level = level();
			if (!level.isClientSide && this.isAlive() && !this.dead && EventHooks.canEntityGrief(level, this)) {
				List<ItemEntity> markedItems = level.getEntitiesOfClass(
						ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D), itemEntity -> {
							if (itemEntity.isRemoved()) return false;
							ItemStack item = itemEntity.getItem();
							if (item.isEmpty()) return false;
							return ItemTracking.isProbablyTracked(item);
						});
				if (!markedItems.isEmpty()) {
					for (ItemEntity itementity : markedItems) {
						if (this.wantsToPickUp(itementity.getItem())) {
							this.pickUpItem(itementity);
						}
					}
				}
			}
		}
	}

}
