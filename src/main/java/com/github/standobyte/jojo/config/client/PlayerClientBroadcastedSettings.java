package com.github.standobyte.jojo.config.client;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

// TODO (client config) attach this to the server players
// TODO (client config) send to server when edited
@Deprecated
public class PlayerClientBroadcastedSettings {
	public HumanoidArm standSide = HumanoidArm.RIGHT;
	public boolean vampireGlowingEyes = true;


	public void toBuf(FriendlyByteBuf buf) {
		buf.writeEnum(standSide);
		buf.writeBoolean(vampireGlowingEyes);
	}

	public void fromBuf(FriendlyByteBuf buf) {
		standSide = buf.readEnum(HumanoidArm.class);
		vampireGlowingEyes = buf.readBoolean();
	}


	public void broadcastToServer() {
//		if (Minecraft.getInstance().getConnection() != null) {
//			PacketDistributor.sendToServer(new ClBroadcastedModSettingsPacket(this));
//		}
	}

	public void syncToAll(Player player) {
//		PacketDistributor.sendToClientsTracking(new TrPlayerModSettingsPacket(player.getId(), this), player);
	}

	public void syncToTracking(Player player, ServerPlayer tracking) {
//		PacketDistributor.sendToClient(new TrPlayerModSettingsPacket(player.getId(), this), tracking);
	}

	public static Optional<PlayerClientBroadcastedSettings> getPlayerSettings(Player player) {
		if (player.isLocalPlayer()) {
			return Optional.of(ClientModSettings.getSettingsReadOnly().broadcasted);
		}
		return Optional.empty();
//		return player.getCapability(PlayerUtilCapProvider.CAPABILITY).map(PlayerUtilCap::getBroadcastedSettings);
	}
}
