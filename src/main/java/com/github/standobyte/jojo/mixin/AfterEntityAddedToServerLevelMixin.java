package com.github.standobyte.jojo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataExtended;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

@Mixin(PersistentEntitySectionManager.class)
public class AfterEntityAddedToServerLevelMixin {

	@Inject(method = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;"
			+ "addEntityWithoutEvent("
			+ "Lnet/minecraft/world/level/entity/EntityAccess;"
			+ "Z)Z", 
			at = @At(value = "INVOKE", 
			target = "Lnet/minecraft/world/level/entity/Visibility;isTicking()Z"))
	public void onAddedToServerLevelAfterSync(EntityAccess _entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> ci) {
		Entity entity = (Entity) _entity;
		ServerLevel level = (ServerLevel) entity.level();
		SynchedDataExtended.syncNonDefaultValues(level, entity);
	}
}
