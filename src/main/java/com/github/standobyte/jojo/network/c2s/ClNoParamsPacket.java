package com.github.standobyte.jojo.network.c2s;

import java.util.Map;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.mechanics.clothes.container.PlayerClothesMenu;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.functions.EnumUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClNoParamsPacket(PacketType packetType) implements CustomPacketPayload {
	private static final Map<PacketType, ClNoParamsPacket> INSTANCES = EnumUtil.makeEnumMap(PacketType.class, ClNoParamsPacket::new);

	public static ClNoParamsPacket of(PacketType packetType) {
		return INSTANCES.get(packetType);
	}

	private static CustomPacketPayload.Type<ClNoParamsPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ClNoParamsPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClNoParamsPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClNoParamsPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<FriendlyByteBuf, ClNoParamsPacket> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(PacketType.class)
				.map(ClNoParamsPacket::of, ClNoParamsPacket::packetType);

		@Override
		public void handle(ClNoParamsPacket payload, IPayloadContext context) {
			Player player = context.player();
			switch (payload.packetType) {
				case SUMMON_STAND -> {
					StandPower standPower = StandPower.get(player);
					if (standPower.hasPower()) {
						StandType standType = standPower.getPowerType();
						standType.onUserSummonCommand(player, standPower);
					}
				}
				case OPEN_CLOTHES -> {
					PlayerClothesMenu.openOnButtonClick((ServerPlayer) player);
				}
			}
		}

	}

	public enum PacketType {
		SUMMON_STAND,
		OPEN_CLOTHES
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
}
