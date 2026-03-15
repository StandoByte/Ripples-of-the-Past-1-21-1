package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.v1_21_4_stuff.missingmethods._Vec3;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrSyncStandOffsetPacket(int standEntityId, Vec3 absoluteOffset, StandOffsetFromUser.Rotations rotations) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrSyncStandOffsetPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrSyncStandOffsetPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrSyncStandOffsetPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrSyncStandOffsetPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrSyncStandOffsetPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrSyncStandOffsetPacket::standEntityId,
				_Vec3.STREAM_CODEC, TrSyncStandOffsetPacket::absoluteOffset,
				NeoForgeStreamCodecs.enumCodec(StandOffsetFromUser.Rotations.class), TrSyncStandOffsetPacket::rotations,
				TrSyncStandOffsetPacket::new);

		@Override
		public void handle(TrSyncStandOffsetPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.standEntityId);
			if (entity instanceof StandEntity stand) {
				stand.offsetFromUser.setOffset(payload.absoluteOffset, payload.rotations);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
