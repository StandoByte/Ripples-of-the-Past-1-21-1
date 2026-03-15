package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrPowerDataPacket implements CustomPacketPayload {
	private final int entityId;
	private final boolean isSentToTracking;
	private final PowerClass<?> powerClass;
	private PowerData serverPowerTypeData;
	private FriendlyByteBuf clientPowerTypeData;
	
	public TrPowerDataPacket(int entityId, PowerClass<?> powerClass, PowerData powerTypeData, boolean isSentToTracking) {
		this.entityId = entityId;
		this.isSentToTracking = isSentToTracking;
		this.powerClass = powerClass;
		this.serverPowerTypeData = powerTypeData;
	}
	
	private TrPowerDataPacket(int entityId, PowerClass<?> powerClass, boolean isSentToTracking) {
		this.entityId = entityId;
		this.isSentToTracking = isSentToTracking;
		this.powerClass = powerClass;
	}
	
	
	private static CustomPacketPayload.Type<TrPowerDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrPowerDataPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrPowerDataPacket> type() {
			return type;
		}

		@Override
		public void encode(TrPowerDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			PowerClass.NETWORK_CODEC.encode(buf, packet.powerClass);
			buf.writeBoolean(packet.isSentToTracking);
			if (packet.serverPowerTypeData != null) {
				packet.serverPowerTypeData.toBuf(buf, packet.isSentToTracking);
			}
		}

		@Override
		public TrPowerDataPacket decode(RegistryFriendlyByteBuf buf) {
			TrPowerDataPacket packet = new TrPowerDataPacket(
					buf.readInt(), 
					PowerClass.NETWORK_CODEC.decode(buf), 
					buf.readBoolean());
			packet.clientPowerTypeData = NetworkUtil.extraPacketData(buf);
			return packet;
		}

		@Override
		public void handle(TrPowerDataPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				Power<?> power = payload.powerClass.get(living);
				if (power != null) {
					PowerData perTypePlayerData = power.getCurTypeData();
					if (perTypePlayerData != null) {
						perTypePlayerData.fromBuf(payload.clientPowerTypeData, payload.isSentToTracking);
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
