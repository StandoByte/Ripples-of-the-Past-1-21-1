package com.github.standobyte.jojo.powersystem.entityaction.netcode;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrEntityActionInstancePacket implements CustomPacketPayload {
	private int performerId;
	@Nullable private EntityActionInstance sendAction;
	@Nullable private FriendlyByteBuf receiveActionData;
	
	public TrEntityActionInstancePacket(int performerId, @Nullable EntityActionInstance action) {
		this.performerId = performerId;
		this.sendAction = action;
	}
	
	
	
	private static CustomPacketPayload.Type<TrEntityActionInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrEntityActionInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrEntityActionInstancePacket> type() {
			return type;
		}

		@Override
		public void encode(TrEntityActionInstancePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.performerId);
			EntityActionInstance.encode(buf, packet.sendAction);
		}

		@Override
		public TrEntityActionInstancePacket decode(RegistryFriendlyByteBuf buf) {
			int performerId = buf.readInt();
			TrEntityActionInstancePacket packet = new TrEntityActionInstancePacket(performerId, null);
			packet.receiveActionData = NetworkUtil.extraPacketData(buf);
			return packet;
		}
		
		@Override
		public void handle(TrEntityActionInstancePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.performerId);
			if (entity instanceof LivingEntity living) {
				EntityActionInstance action = EntityActionInstance.decode(entity.level(), payload.receiveActionData);
				LivingComponentAction.getComponent(living).setAction(action, SyncType.NO_SYNC);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
