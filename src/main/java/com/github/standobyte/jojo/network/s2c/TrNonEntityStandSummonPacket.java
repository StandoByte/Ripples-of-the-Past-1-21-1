package com.github.standobyte.jojo.network.s2c;

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

public record TrNonEntityStandSummonPacket(int userId, boolean summoned) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrNonEntityStandSummonPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrNonEntityStandSummonPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrNonEntityStandSummonPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrNonEntityStandSummonPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrNonEntityStandSummonPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrNonEntityStandSummonPacket::userId,
				ByteBufCodecs.BOOL, TrNonEntityStandSummonPacket::summoned,
				TrNonEntityStandSummonPacket::new);

		@Override
		public void handle(TrNonEntityStandSummonPacket payload, IPayloadContext context) {
			Entity userEntity = ClientProxy.getEntityById(payload.userId);
			if (userEntity instanceof LivingEntity userLiving) {
				StandPower standPower = StandPower.get(userLiving);
				if (standPower != null && standPower.hasPower()) {
					if (payload.summoned) {
						standPower.getPowerType().summon(userLiving, standPower);
					}
					else {
						standPower.getPowerType().forceUnsummon(userLiving, standPower);
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
