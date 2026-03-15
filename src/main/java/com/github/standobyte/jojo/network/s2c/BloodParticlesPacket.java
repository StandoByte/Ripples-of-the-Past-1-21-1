package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;
import java.util.Random;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Vec3;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BloodParticlesPacket(Vec3 posSource, Optional<Vec3> posDest, float speed, int count, int entityId) implements CustomPacketPayload {

	public BloodParticlesPacket(Vec3 posSource, float speed, int count, int entityId) {
		this(posSource, Optional.empty(), speed, count, entityId);
	}

	public BloodParticlesPacket(Vec3 posSource, Vec3 posDest, float speed, int count, int entityId) {
		this(posSource, Optional.of(posDest), speed, count, entityId);
	}

	private static CustomPacketPayload.Type<BloodParticlesPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<BloodParticlesPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<BloodParticlesPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, BloodParticlesPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, BloodParticlesPacket> STREAM_CODEC = StreamCodec.composite(
				_Vec3.STREAM_CODEC, BloodParticlesPacket::posSource,
				_Vec3.STREAM_CODEC.apply(ByteBufCodecs::optional), BloodParticlesPacket::posDest,
				ByteBufCodecs.FLOAT, BloodParticlesPacket::speed,
				ByteBufCodecs.INT, BloodParticlesPacket::count,
				ByteBufCodecs.INT, BloodParticlesPacket::entityId,
				BloodParticlesPacket::new);

        private static final Random RANDOM = new Random();
		@Override
		public void handle(BloodParticlesPacket payload, IPayloadContext context) {
			Optional<Vec3> diff = payload.posDest.map(vec -> vec.subtract(payload.posSource).normalize().scale(payload.speed));
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			for (int i = 0; i < payload.count; i++) {
				Vec3 speedVec = diff.orElseGet(() -> {
					float xRot = (RANDOM.nextFloat() - 0.5f) * (float) Math.PI;
					float yRot = RANDOM.nextFloat() * (float) Math.PI * 2;
					return MathUtil.vecFromAngles(xRot, yRot).scale(payload.speed);
				});
				CustomParticlesHelper.createBloodParticle(ModParticles.BLOOD.get(), entity, 
						payload.posSource.x, payload.posSource.y, payload.posSource.z, 
						speedVec.x, speedVec.y, speedVec.z);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
