package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.syncfix;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.network.s2c.EntitySyncMotionBypassingPacket;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(ServerEntity.class)
public class ServerBypassSyncMotionMixin {
	@Final private Entity entity;

	@Inject(method = "sendChanges", at = @At(
			value = "FIELD", 
			target =  "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", 
			ordinal = 0))
	public void jojo_ripples$syncHurtMarkedFlag(CallbackInfo ci) {
		if (entity.hurtMarked && EntityComponentController.getCurrentController(entity) != null) {
			PacketDistributor.sendToPlayersTrackingEntity(this.entity, new EntitySyncMotionBypassingPacket(this.entity.getId(), this.entity.getDeltaMovement()));
		}
	}
}
