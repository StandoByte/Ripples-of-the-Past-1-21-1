package com.github.standobyte.jojo.mechanics.jojopose;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.init.ModSpecialActions;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.entityaction.netcode.SyncType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClJojoPoseActionPacket implements CustomPacketPayload {
	private final PacketType packetType;
	private final ResourceLocation animationSet;
	private final String animName;
	private final int animIndex;
	
	public static ClJojoPoseActionPacket start(ResourceLocation animationSet, String animName, int animIndex) {
		return new ClJojoPoseActionPacket(PacketType.START, animationSet, animName, animIndex);
	}
	
	public static ClJojoPoseActionPacket stop() {
		return new ClJojoPoseActionPacket(PacketType.STOP, null, null, 0);
	}
	
	private ClJojoPoseActionPacket(PacketType packetType, 
			ResourceLocation animationSet, String animName, int animIndex) {
		this.packetType = packetType;
		this.animationSet = animationSet;
		this.animName = animName;
		this.animIndex = animIndex;
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
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case START -> {
					ResourceLocation.STREAM_CODEC.encode(buf, packet.animationSet);
					buf.writeUtf(packet.animName);
					buf.writeVarInt(packet.animIndex);
				}
				case STOP -> {}
			}
		}

		@Override
		public ClJojoPoseActionPacket decode(RegistryFriendlyByteBuf buf) {
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case START -> {
					ResourceLocation animationSet = ResourceLocation.STREAM_CODEC.decode(buf);
					String animName = buf.readUtf();
					int animIndex = buf.readVarInt();
					yield start(animationSet, animName, animIndex);
				}
				case STOP -> stop();
			};
		}

		@Override
		public void handle(ClJojoPoseActionPacket payload, IPayloadContext context) {
			Player player = context.player();
			switch (payload.packetType) {
				case START -> {
					LivingComponentAction actionComponent = LivingComponentAction.getComponent(player);
					EntityActionInstance poseAction = new JojoPoseActionType.PosingInstance(
							payload.animationSet, payload.animName, payload.animIndex);
					actionComponent.setAction(poseAction, SyncType.TRACKING_AND_SELF);
				}
				case STOP -> {
					LivingComponentAction actionComponent = LivingComponentAction.getComponent(player);
					EntityActionInstance curAction = actionComponent.getAction();
					if (curAction != null && curAction.ability == ModSpecialActions.JOJO_POSE.get()) {
						curAction.forceStop();
						curAction.syncPhaseChanges();
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
