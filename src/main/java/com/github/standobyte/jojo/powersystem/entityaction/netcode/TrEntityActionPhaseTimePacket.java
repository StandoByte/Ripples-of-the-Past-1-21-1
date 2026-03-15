package com.github.standobyte.jojo.powersystem.entityaction.netcode;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrEntityActionPhaseTimePacket(int performerId, 
		int actionId, 
		Object2FloatMap<ActionPhase> phasesLength, 
		ActionPhase phase, 
		int curPhaseTick) implements CustomPacketPayload {
	
	private static CustomPacketPayload.Type<TrEntityActionPhaseTimePacket> type;
	
	public static class Handler implements PacketsRegister.PacketCodecHandler<TrEntityActionPhaseTimePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrEntityActionPhaseTimePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, TrEntityActionPhaseTimePacket> reader() {
			return STREAM_CODEC;
		}
		
		
		public static final StreamCodec<RegistryFriendlyByteBuf, TrEntityActionPhaseTimePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, TrEntityActionPhaseTimePacket::performerId,
				ByteBufCodecs.VAR_INT, TrEntityActionPhaseTimePacket::actionId,
				ByteBufCodecs.map(
						size -> new Object2FloatArrayMap<>(), 
						NeoForgeStreamCodecs.enumCodec(ActionPhase.class), 
						ByteBufCodecs.FLOAT), TrEntityActionPhaseTimePacket::phasesLength,
				NeoForgeStreamCodecs.enumCodec(ActionPhase.class).apply(NetworkUtil::nullableCodec), TrEntityActionPhaseTimePacket::phase,
				ByteBufCodecs.VAR_INT, TrEntityActionPhaseTimePacket::curPhaseTick,
				TrEntityActionPhaseTimePacket::new);

		@Override
		public void handle(TrEntityActionPhaseTimePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.performerId);
			if (entity instanceof LivingEntity living) {
				EntityActionInstance action = LivingComponentAction.getCurEntityAction(living);
				if (action != null && action.id == payload.actionId) {
					action.phasesLength = payload.phasesLength;
					action.setPhase(payload.phase, payload.curPhaseTick);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
