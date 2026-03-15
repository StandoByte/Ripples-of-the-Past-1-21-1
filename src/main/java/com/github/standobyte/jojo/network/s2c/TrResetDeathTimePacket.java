package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrResetDeathTimePacket(int entityId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrResetDeathTimePacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrResetDeathTimePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrResetDeathTimePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrResetDeathTimePacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrResetDeathTimePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrResetDeathTimePacket::entityId,
				TrResetDeathTimePacket::new);

		@Override
		public void handle(TrResetDeathTimePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				if (living.isDeadOrDying()) {/*  new health value might not yet sync at this point, 
											  *  which will cause the deathTime timer to tick up a bit more
											  *  unless i do smth like this
											  */
					living.setHealth(0.0001F);
				}
				living.deathTime = 0;
            }
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
