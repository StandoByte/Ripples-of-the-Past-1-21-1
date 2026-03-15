package com.github.standobyte.jojo.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.network.s2c.EntitySyncMotionBypassingPacket;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;

@Mixin(ServerEntity.class)
public class ProjectileHighSpeedSync {
	private boolean sentMovementPacket;

	@Shadow @Final private Entity entity;
//	@Shadow private Vec3 lastSentMovement;
//	@Shadow @Final private boolean trackDelta;
//	@Shadow private int tickCount;
//	@Shadow @Final private int updateInterval;


	@Inject(method = "sendChanges", at = @At("HEAD"))
	public void jojo_ripples$onUpdateStart(CallbackInfo ci) {
		sentMovementPacket = this.entity.hurtMarked;
	}

	@Inject(method = "sendChanges", at = @At(value = "INVOKE", target = 
			"Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;<init>(ILnet/minecraft/world/phys/Vec3;)V"))
	public void jojo_ripples$onSpeedUpdate(CallbackInfo ci) {
		this.sentMovementPacket = true;
	}

	@Inject(method = "sendChanges", at = @At("TAIL"))
	public void jojo_ripples$onUpdateEnd(CallbackInfo ci) {
		if (sentMovementPacket) {
			syncSpeed(packet -> PacketDistributor.sendToPlayersTrackingEntity(this.entity, packet));
		}
	}

	@Inject(method = "sendPairingData", at = @At("TAIL"))
	public void jojo_ripples$onStartedTracking(ServerPlayer player, PacketAndPayloadAcceptor<ClientGamePacketListener> bundle, CallbackInfo ci) {
		syncSpeed(bundle::accept);
	}

	public void syncSpeed(Consumer<CustomPacketPayload> send) {
		Vec3 deltaMovement = entity.getDeltaMovement();
		if (Math.abs(deltaMovement.x) > 3.9 || Math.abs(deltaMovement.y) > 3.9 || Math.abs(deltaMovement.z) > 3.9) {
			CustomPacketPayload packet = new EntitySyncMotionBypassingPacket(entity.getId(), deltaMovement);
			send.accept(packet);
		}
	}

}
