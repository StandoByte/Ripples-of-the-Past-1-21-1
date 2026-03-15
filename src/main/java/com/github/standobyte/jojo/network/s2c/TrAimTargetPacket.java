package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrAimTargetPacket implements CustomPacketPayload {
	private final int entityId;
	private final ActionTarget target;
	
	public TrAimTargetPacket(int entityId, ActionTarget target) {
		this.entityId = entityId;
		this.target = target;
	}

	
	
	private static CustomPacketPayload.Type<TrAimTargetPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrAimTargetPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrAimTargetPacket> type() {
			return type;
		}

		@Override
		public void encode(TrAimTargetPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID.encode(buf, packet.target);
		}

		@Override
		public TrAimTargetPacket decode(RegistryFriendlyByteBuf buf) {
			return new TrAimTargetPacket(buf.readInt(), ActionTarget.STREAM_CODEC_UNRESOLVED_ENTITY_ID.decode(buf));
		}

		@Override
		public void handle(TrAimTargetPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				LivingComponentAction entityActionComponent = LivingComponentAction.getComponent(living);
				if (entityActionComponent != null) {
					ActionTarget target = payload.target.resolveEntityId(entity.level());
					entityActionComponent.entityAim.setTarget(target);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
