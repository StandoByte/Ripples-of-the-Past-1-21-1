package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.customobjects.entity_projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ModdedProjectileBreakPacket(int entityId, ActionTarget target) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<ModdedProjectileBreakPacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<ModdedProjectileBreakPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ModdedProjectileBreakPacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ModdedProjectileBreakPacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, ModdedProjectileBreakPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, ModdedProjectileBreakPacket::entityId,
				ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID, ModdedProjectileBreakPacket::target,
				ModdedProjectileBreakPacket::new);

		@Override
		public void handle(ModdedProjectileBreakPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof ModdedProjectileEntity projectile) {
				ActionTarget target = payload.target.resolveEntityId(entity.level());
				projectile.clientBreakProjectile(target);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
