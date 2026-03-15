package com.github.standobyte.jojo.mixin.itemtracking.track.inventory;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(SimpleContainer.class)
public abstract class HorseInventoryMixin implements Container {
	@Shadow @Final private NonNullList<ItemStack> items;
	@Shadow private List<ContainerListener> listeners;

	@Inject(method = "setItem", at = @At("TAIL"))
	public void jojo_ripples$onItemSetToSlot(int slot, ItemStack item, CallbackInfo ci) {
		if (listeners != null) {
			for (ContainerListener shouldBeHorse : listeners) {
				if (shouldBeHorse instanceof AbstractHorse horse) {
					Level level = horse.level();
					if (!level.isClientSide()) {
						ItemTracker tracker = ItemTracking.getItemTracker(item, level);
						if (tracker != null) {
							tracker.setAtEntity(item, horse.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
									items.stream().anyMatch(ItemTracking.trackerIdCheck(trackerId)));
						}
					}
					break;
				}
			}
		}
	}
}
