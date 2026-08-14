package com.github.standobyte.jojo.mechanics.jojopose;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClJojoPoseActionPacket implements CustomPacketPayload {
	private final int entityId;
	private final PacketType packetType;
	private final ResourceLocation animationSet;
	private final String animName;
	
	public static ClJojoPoseActionPacket start(int entityId, ResourceLocation animationSet, String animName) {
		return new ClJojoPoseActionPacket(entityId, PacketType.START, animationSet, animName);
	}
	
	public static ClJojoPoseActionPacket stop(int entityId) {
		return new ClJojoPoseActionPacket(entityId, PacketType.STOP, null, null);
	}
	
	private ClJojoPoseActionPacket(int entityId, PacketType packetType, 
			ResourceLocation animationSet, String animName) {
		this.entityId = entityId;
		this.packetType = packetType;
		this.animationSet = animationSet;
		this.animName = animName;
	}

	enum PacketType {
		START,
		STOP
	}

	
	
	private static CustomPacketPayload.Type<ClJojoPoseActionPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClJojoPoseActionPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClJojoPoseActionPacket> type() {
			return type;
		}

		@Override
		public void encode(ClJojoPoseActionPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case START -> {
					ResourceLocation.STREAM_CODEC.encode(buf, packet.animationSet);
					buf.writeUtf(packet.animName);
				}
				case STOP -> {}
			}
		}

		@Override
		public ClJojoPoseActionPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case START -> {
					ResourceLocation animationSet = ResourceLocation.STREAM_CODEC.decode(buf);
					String animName = buf.readUtf();
					yield start(entityId, animationSet, animName);
				}
				case STOP -> stop(entityId);
			};
		}

		@Override
		public void handle(ClJojoPoseActionPacket payload, IPayloadContext context) {
			Player player = context.player();
			LivingEntity posingEntity = null;
			if (player.getId() == payload.entityId) {
				posingEntity = player;
			}
			if (posingEntity == null) {
				Entity entity = player.level().getEntity(payload.entityId);
				if (entity instanceof LivingEntity living && 
						living.getType() == ModEntityTypes.MANNEQUIN.get() && living.distanceToSqr(player) < 16) {
					posingEntity = living;
				}
			}
			
			if (posingEntity != null) {
				switch (payload.packetType) {
					case START -> {
						LivingComponentAction actionComponent = LivingComponentAction.getComponent(posingEntity);
						EntityActionInstance poseAction = new JojoPoseActionType.PosingInstance(
								payload.animationSet, payload.animName);
						actionComponent.setAction(poseAction, SyncType.TRACKING_AND_SELF);
					}
					case STOP -> {
						LivingComponentAction actionComponent = LivingComponentAction.getComponent(posingEntity);
						EntityActionInstance curAction = actionComponent.getAction();
						if (curAction != null && curAction.ability == ModSpecialActions.JOJO_POSE.get()) {
							curAction.forceStop();
							curAction.syncPhaseChanges();
						}
					}
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
