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

public record EntitySyncMotionBypassingPacket(int entityId, Vec3 deltaMovement) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<EntitySyncMotionBypassingPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<EntitySyncMotionBypassingPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<EntitySyncMotionBypassingPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, EntitySyncMotionBypassingPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, EntitySyncMotionBypassingPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, EntitySyncMotionBypassingPacket::entityId,
				_Vec3.STREAM_CODEC, EntitySyncMotionBypassingPacket::deltaMovement,
				EntitySyncMotionBypassingPacket::new);

		@Override
		public void handle(EntitySyncMotionBypassingPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity != null) {
				Vec3 vec = payload.deltaMovement;
				entity.lerpMotion(vec.x, vec.y, vec.z);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
