package com.github.standobyte.jojo.mixin.itemtracking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracker;
import com.github.standobyte.jojo.mechanics.itemtracking.ItemTracking;
import com.github.standobyte.jojo.mechanics.itemtracking.KnownItemState;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Shadow private Entity entityRepresentation;

	@Inject(method = "setEntityRepresentation", at = @At("HEAD"))
	public void onSetEntityRepresentation(Entity entity, CallbackInfo ci) {
		if (entity != null) {
			Level level = entity.level();
			if (level != null && !level.isClientSide()) {
				ItemStack asItem = (ItemStack) (Object) this;
				ItemTracker tracker = ItemTracking.getItemTracker(asItem, level);
				if (tracker != null) {
					tracker.setAtEntity(asItem, entity.getId(), level, entity instanceof ItemEntity ? KnownItemState.ENTITY_IS_ITEM : KnownItemState.ENTITY_HAS_ITEM);
					tracker.setItemStillThereCheck(trackerId -> this.entityRepresentation == entity);
				}
			}
		}
	}

}
