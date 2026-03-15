package com.github.standobyte.jojo.subsystems.entity_puppetcontrol;

import java.util.Optional;

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

public record SetClientControllerPacket(int targetId, int controllerId, Optional<String> controllerType) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<SetClientControllerPacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<SetClientControllerPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<SetClientControllerPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, SetClientControllerPacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<RegistryFriendlyByteBuf, SetClientControllerPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, SetClientControllerPacket::targetId,
				ByteBufCodecs.INT, SetClientControllerPacket::controllerId,
				ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), SetClientControllerPacket::controllerType,
				SetClientControllerPacket::new);

		@Override
		public void handle(SetClientControllerPacket packet, IPayloadContext context) {
			Entity target = ClientProxy.getEntityById(packet.targetId);
			Entity ctrlEntity = ClientProxy.getEntityById(packet.controllerId);
			
			if (ctrlEntity instanceof LivingEntity controller) {
				EntityComponentController.setControlTarget(controller, target, packet.controllerType.orElse(null));
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
