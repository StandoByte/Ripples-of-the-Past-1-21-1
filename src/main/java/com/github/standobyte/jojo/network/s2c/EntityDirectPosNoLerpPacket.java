package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EntityDirectPosNoLerpPacket(int entityId, Vec3 pos) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<EntityDirectPosNoLerpPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<EntityDirectPosNoLerpPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<EntityDirectPosNoLerpPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, EntityDirectPosNoLerpPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, EntityDirectPosNoLerpPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, EntityDirectPosNoLerpPacket::entityId,
				_Vec3.STREAM_CODEC, EntityDirectPosNoLerpPacket::pos,
				EntityDirectPosNoLerpPacket::new);

		@Override
		public void handle(EntityDirectPosNoLerpPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity != null) {
				entity.setPos(payload.pos);
				entity.xo = payload.pos.x;
				entity.yo = payload.pos.y;
				entity.zo = payload.pos.z;
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
