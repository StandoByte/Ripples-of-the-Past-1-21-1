package com.github.standobyte.jojo.core.packet.fromserver;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.util.network.StreamCodecs;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ItemBreakVisualsPacket(Vec3 pos, Item item) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<ItemBreakVisualsPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ItemBreakVisualsPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ItemBreakVisualsPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, ItemBreakVisualsPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ItemBreakVisualsPacket> STREAM_CODEC = StreamCodec.composite(
				StreamCodecs.VEC_3D_APPROX, ItemBreakVisualsPacket::pos, 
				ByteBufCodecs.registry(Registries.ITEM), ItemBreakVisualsPacket::item, 
				ItemBreakVisualsPacket::new);

		@Override
		public void handle(ItemBreakVisualsPacket payload, IPayloadContext context) {
			Vec3 pos = payload.pos;
			Level clientLevel = ClientProxy.getClientWorld();
			RandomSource random = clientLevel.random;
			clientLevel.playLocalSound(pos.x, pos.y, pos.z, 
					payload.item.getBreakingSound(), SoundSource.AMBIENT,
					0.8F, 0.8F + random.nextFloat() * 0.4F, false);

			for (int i = 0; i < 25; i++) {
				float xRot = (random.nextFloat() - 0.5f) * (float) Math.PI;
				float yRot = random.nextFloat() * (float) Math.PI * 2;
				Vec3 particleSpeed = new Vec3(
						(random.nextFloat() - 0.5) * 0.05, 
						Math.random() * 0.1 + 0.1, 
						0.0);
				particleSpeed = particleSpeed.xRot(xRot);
				particleSpeed = particleSpeed.yRot(yRot);

				Vec3 particlePos = new Vec3(
						(random.nextFloat() - 0.5) * 0.15, 
						(-random.nextFloat()) * 0.6 - 0.3, 
						0.3);
				particlePos = particlePos.xRot(yRot);
				particlePos = particlePos.yRot(yRot);
				particlePos = particlePos.add(pos.x, pos.y, pos.z);
				clientLevel.addParticle(new ItemParticleOption(ParticleTypes.ITEM, payload.item.getDefaultInstance()), 
						particlePos.x, particlePos.y, particlePos.z, 
						particleSpeed.x, particleSpeed.y + 0.05, particleSpeed.z);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
