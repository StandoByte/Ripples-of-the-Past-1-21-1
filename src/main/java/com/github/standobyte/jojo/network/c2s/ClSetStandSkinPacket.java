package com.github.standobyte.jojo.network.c2s;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClSetStandSkinPacket(Optional<ResourceLocation> standSkin, ResourceLocation standId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ClSetStandSkinPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ClSetStandSkinPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClSetStandSkinPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClSetStandSkinPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ClSetStandSkinPacket> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), ClSetStandSkinPacket::standSkin,
				ResourceLocation.STREAM_CODEC, ClSetStandSkinPacket::standId,
				ClSetStandSkinPacket::new);

		@Override
		public void handle(ClSetStandSkinPacket payload, IPayloadContext context) {
			Player player = context.player();
			StandPower standPower = StandPower.get(player);
			if (standPower != null) {
				standPower.setSelectedSkin(payload.standSkin);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
