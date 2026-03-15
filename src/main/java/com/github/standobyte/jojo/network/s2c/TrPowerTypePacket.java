package com.github.standobyte.jojo.network.s2c;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrPowerTypePacket(int entityId, @Nullable PlayerPowerType<?> powerType) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrPowerTypePacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrPowerTypePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrPowerTypePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrPowerTypePacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrPowerTypePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrPowerTypePacket::entityId,
				NetworkUtil.nullableCodec(NetworkUtil.registryCodec(JojoRegistries.PLAYER_POWER_TYPES_REG_KEY)), TrPowerTypePacket::powerType,
				TrPowerTypePacket::new);

		@Override
		public void handle(TrPowerTypePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				PlayerPower.getOptional(living).ifPresent(power -> power.setPowerType(payload.powerType()));
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
