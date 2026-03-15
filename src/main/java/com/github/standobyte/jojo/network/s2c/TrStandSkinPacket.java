package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrStandSkinPacket(int userId, Optional<ResourceLocation> skinId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrStandSkinPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrStandSkinPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrStandSkinPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrStandSkinPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrStandSkinPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrStandSkinPacket::userId,
				ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), TrStandSkinPacket::skinId,
				TrStandSkinPacket::new);

		@Override
		public void handle(TrStandSkinPacket payload, IPayloadContext context) {
			Entity userEntity = ClientProxy.getEntityById(payload.userId);
			if (userEntity instanceof LivingEntity userLiving) {
				StandPower standPower = StandPower.get(userLiving);
				if (standPower != null) {
					standPower.setSelectedSkin(payload.skinId);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
