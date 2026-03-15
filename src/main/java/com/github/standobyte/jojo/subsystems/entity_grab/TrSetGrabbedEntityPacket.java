package com.github.standobyte.jojo.subsystems.entity_grab;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrSetGrabbedEntityPacket(int grabbingId, int grabbedId) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrSetGrabbedEntityPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrSetGrabbedEntityPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrSetGrabbedEntityPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrSetGrabbedEntityPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrSetGrabbedEntityPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrSetGrabbedEntityPacket::grabbingId,
				ByteBufCodecs.INT, TrSetGrabbedEntityPacket::grabbedId,
				TrSetGrabbedEntityPacket::new);

		@Override
		public void handle(TrSetGrabbedEntityPacket payload, IPayloadContext context) {
			if (ClientProxy.getEntityById(payload.grabbingId) instanceof LivingEntity grabbingEntity) {
				grabbingEntity
				.getData(ModDataAttachmentTypes.LIVING_GRAB.get())
				.setGrabTarget(ClientProxy.getEntityById(payload.grabbedId) instanceof LivingEntity grabbedEntity ? grabbedEntity : null);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
