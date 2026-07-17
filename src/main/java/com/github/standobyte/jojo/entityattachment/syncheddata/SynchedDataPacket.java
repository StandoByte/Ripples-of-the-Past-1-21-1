package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SynchedDataPacket implements CustomPacketPayload {
	private final int entityId;
	private final String synchedDataType;
	private final List<SynchedEntityData.DataValue<?>> packedItems;
	
	public SynchedDataPacket(int entityId, String synchedDataType, List<SynchedEntityData.DataValue<?>> packedItems) {
		this.entityId = entityId;
		this.synchedDataType = synchedDataType;
		this.packedItems = packedItems;
	}
	
	
	private static CustomPacketPayload.Type<SynchedDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<SynchedDataPacket> {
		public static Map<String, SynchedDataPacketHandler> specificHandlers = new HashMap<>();
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<SynchedDataPacket> type() {
			return type;
		}

		@Override
		public void encode(SynchedDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeUtf(packet.synchedDataType);
			
			for (SynchedEntityData.DataValue<?> datavalue : packet.packedItems) {
				datavalue.write(buf);
			}
			buf.writeByte(255);
		}

		@Override
		public SynchedDataPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			String type = buf.readUtf();

			List<SynchedEntityData.DataValue<?>> packedItems = new ArrayList<>();
			int id;
			while ((id = buf.readUnsignedByte()) != 255) {
				packedItems.add(SynchedEntityData.DataValue.read(buf, id));
			}

			return new SynchedDataPacket(entityId, type, packedItems);
		}

		@Override
		public void handle(SynchedDataPacket payload, IPayloadContext context) {
			SynchedDataPacketHandler handler = specificHandlers.get(payload.synchedDataType);
			if (handler != null) {
				Entity entity = ClientProxy.getEntityById(payload.entityId);
				if (entity != null) {
					SynchedDataHelper dataObj = handler.getDataSyncHelper(entity);
					if (dataObj != null) {
						dataObj.getDataSyncher().assignValues(payload.packedItems);
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
