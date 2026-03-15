package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.syncfix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Shadow private ClientLevel level;

	@Inject(method = "handleMoveEntity", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelMoveEntityPacket(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
		ClientEntityController controller = ClientEntityController.getInstance();
		if (controller != null) {
			Entity entity = packet.getEntity(this.level);
			if (controller.isBeingControlled(entity)) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "handleRotateMob", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelRotateHeadPacket(ClientboundRotateHeadPacket packet, CallbackInfo ci) {
		ClientEntityController controller = ClientEntityController.getInstance();
		if (controller != null) {
			Entity entity = packet.getEntity(this.level);
			if (controller.isBeingControlled(entity)) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
	public void jojo_ripples$cancelSetEntityMotionPacket(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
		ClientEntityController controller = ClientEntityController.getInstance();
		if (controller != null) {
			Entity entity = this.level.getEntity(packet.getId());
			if (controller.isBeingControlled(entity)) {
				ci.cancel();
			}
		}
	}
}
