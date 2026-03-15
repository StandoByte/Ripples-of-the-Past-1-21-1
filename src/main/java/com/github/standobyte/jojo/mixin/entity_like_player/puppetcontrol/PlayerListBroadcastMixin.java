package com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.EntityComponentController;

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(PlayerList.class)
public class PlayerListBroadcastMixin {
	@Shadow @Final private List<ServerPlayer> players;

	@Inject(method = "broadcast", at = @At("TAIL"))
	public void jojo_ripples$broadcastToControllers(@Nullable Player except, double x, double y, double z, 
			double radius, ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo ci) {
		double radiusSq = radius * radius;
		for (int i = 0; i < players.size(); i++) {
			ServerPlayer player = players.get(i);
			if (player != except && player.level().dimension() == dimension) {
				double xD = x - player.getX();
				double yD = y - player.getY();
				double zD = z - player.getZ();
				if (xD * xD + yD * yD + zD * zD >= radiusSq) {
					Entity cameraEntity = EntityComponentController.getControlTarget(player);
					if (cameraEntity != null) {
						xD = x - cameraEntity.getX();
						yD = y - cameraEntity.getY();
						zD = z - cameraEntity.getZ();
						if (xD * xD + yD * yD + zD * zD < radiusSq) {
							player.connection.send(packet);
						}
					}
				}
			}
		}
	}
}
