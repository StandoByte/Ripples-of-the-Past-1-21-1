package com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand.input;

import java.util.EnumMap;
import java.util.Map;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClStandItemInputPacket(StandItemInput.Action packetType) implements CustomPacketPayload {

	private static Map<StandItemInput.Action, ClStandItemInputPacket> instances = new EnumMap<>(StandItemInput.Action.class);
	public static ClStandItemInputPacket packet(StandItemInput.Action type) {
		return instances.computeIfAbsent(type, ClStandItemInputPacket::new);
	}



	private static CustomPacketPayload.Type<ClStandItemInputPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<ClStandItemInputPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClStandItemInputPacket> type() {
			return type;
		}

		@Override
		public void encode(ClStandItemInputPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.packetType);
		}

		@Override
		public ClStandItemInputPacket decode(RegistryFriendlyByteBuf buf) {
			return packet(buf.readEnum(StandItemInput.Action.class));
		}

		@Override
		public void handle(ClStandItemInputPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			player.resetLastActionTime();

			StandPower standPower = StandPower.get(player);
			if (standPower != null) {
				StandEntity standEntity = standPower.getSummonedStandEntity();
				if (standEntity != null) {
					StandItemInput.handlePacket(payload.packetType, standEntity, player);
				}
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
