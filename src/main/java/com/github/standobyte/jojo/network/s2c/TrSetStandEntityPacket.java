package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrSetStandEntityPacket(int userId, int standEntityId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrSetStandEntityPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrSetStandEntityPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrSetStandEntityPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrSetStandEntityPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrSetStandEntityPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrSetStandEntityPacket::userId,
				ByteBufCodecs.INT, TrSetStandEntityPacket::standEntityId,
				TrSetStandEntityPacket::new);

		@Override
		public void handle(TrSetStandEntityPacket payload, IPayloadContext context) {
			Entity userEntity = ClientProxy.getEntityById(payload.userId);
			if (userEntity instanceof LivingEntity userLiving) {
				StandPower standPower = StandPower.get(userLiving);
				if (standPower != null) {
					if (payload.standEntityId <= 0) {
						standPower.setSummonedStand(null);
					}
					else {
						Entity entity = ClientProxy.getEntityById(payload.standEntityId);
						if (entity instanceof StandEntity) {
							StandEntity stand = (StandEntity) entity;
							standPower.setSummonedStand(stand);
						}
					}
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
