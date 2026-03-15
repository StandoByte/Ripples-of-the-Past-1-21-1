package com.github.standobyte.jojo.mixin.itemtracking.track.equip;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracker;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.subsystems.itemtracking.KnownItemState;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(AbstractChestedHorse.class)
public abstract class ChestedHorseMixin extends AbstractHorse {

	protected ChestedHorseMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
		super(entityType, level);
	}
	
	@Shadow public abstract boolean hasChest();

	@Inject(method = "equipChest", at = @At("HEAD"))
	public void jojo_ripples$onEquipChest(Player player, ItemStack chestStack, CallbackInfo ci) {
		Level level = level();
		if (!level.isClientSide()) {
			ItemTracker tracker = ItemTracking.getItemTracker(chestStack, level);
			if (tracker != null) {
				ItemStack item = chestStack.copyWithCount(1);
				tracker.setAtEntity(item, this.getId(), level, KnownItemState.ENTITY_HAS_ITEM, trackerId -> 
							this.hasChest());
			}
		}
	}
	
	// XXX (item tracking) dropEquipment - if the chest had a tracker, drop the item with the tracker id
}
