package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.customobjects.entity_projectile.ModdedProjectileEntity;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Vec3;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeflectedBulletPacket(int entityId, Vec3 deflectVec, Vec3 deflectedPos, Vec3 bulletPos) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<DeflectedBulletPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<DeflectedBulletPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<DeflectedBulletPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, DeflectedBulletPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, DeflectedBulletPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, DeflectedBulletPacket::entityId,
				_Vec3.STREAM_CODEC, DeflectedBulletPacket::deflectVec,
				_Vec3.STREAM_CODEC, DeflectedBulletPacket::deflectedPos,
				_Vec3.STREAM_CODEC, DeflectedBulletPacket::bulletPos,
				DeflectedBulletPacket::new);

		@Override
		public void handle(DeflectedBulletPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof ModdedProjectileEntity modProjectile) {
				entity.syncPacketPositionCodec(payload.bulletPos.x, payload.bulletPos.y, payload.bulletPos.z);
				entity.xo = payload.deflectVec.x;
				entity.yo = payload.deflectVec.y;
				entity.zo = payload.deflectVec.z;
				entity.xOld = payload.deflectVec.x;
				entity.yOld = payload.deflectVec.y;
				entity.zOld = payload.deflectVec.z;
				entity.setPos(payload.bulletPos.x, payload.bulletPos.y, payload.bulletPos.z);
				modProjectile.setIsDeflected(payload.deflectVec, payload.deflectedPos);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
