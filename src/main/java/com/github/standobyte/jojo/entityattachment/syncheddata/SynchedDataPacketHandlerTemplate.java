package com.github.standobyte.jojo.entityattachment.syncheddata;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public abstract class SynchedDataPacketHandlerTemplate implements PacketsRegister.PacketOGHandler<SynchedDataPacketHandlerTemplate.SynchedDataPacket> {
	protected final CustomPacketPayload.Type<SynchedDataPacket> type;

	public SynchedDataPacketHandlerTemplate(ResourceLocation packetId) { 
		type = new CustomPacketPayload.Type<>(packetId);
	}
	
	public void onStartedTracking(SynchedDataExtended synchedData, ServerPlayer tracking, Entity entity) {
		List<SynchedEntityData.DataValue<?>> nonDefaultData = synchedData.syncOnStartedTracking();
		if (nonDefaultData != null) {
			PacketDistributor.sendToPlayer(tracking, this.makePacket(entity.getId(), nonDefaultData));
		}
	}
	
	public void tickSyncDirtyData(SynchedDataExtended synchedData, Entity entity) {
		List<SynchedEntityData.DataValue<?>> dirtyData = synchedData.syncDirtyData();
		if (dirtyData != null) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, this.makePacket(entity.getId(), dirtyData));
		}
	}
	
	public SynchedDataPacket makePacket(int entityId, List<DataValue<?>> packedItems) {
		return new SynchedDataPacket(entityId, packedItems, this);
	}
	

	@Override
	public Type<SynchedDataPacket> type() {
		return type;
	}

	@Override
	public void encode(SynchedDataPacket packet, RegistryFriendlyByteBuf buf) {
		buf.writeInt(packet.entityId);

		for (SynchedEntityData.DataValue<?> datavalue : packet.packedItems) {
			datavalue.write(buf);
		}
		buf.writeByte(255);
	}

	@Override
	public SynchedDataPacket decode(RegistryFriendlyByteBuf buf) {
		int entityId = buf.readInt();

		List<SynchedEntityData.DataValue<?>> packedItems = new ArrayList<>();
		int id;
		while ((id = buf.readUnsignedByte()) != 255) {
			packedItems.add(SynchedEntityData.DataValue.read(buf, id));
		}

		return new SynchedDataPacket(entityId, packedItems, this);
	}

	@Override
	public void handle(SynchedDataPacket payload, IPayloadContext context) {
		Entity entity = ClientProxy.getEntityById(payload.entityId);
		if (entity != null) {
			handle(entity, payload.packedItems, payload, context);
		}
	}

	public abstract void handle(Entity entity, List<SynchedEntityData.DataValue<?>> packedItems, 
			SynchedDataPacket payload, IPayloadContext context);

	public static class SynchedDataPacket implements CustomPacketPayload {
		public final int entityId;
		public final List<SynchedEntityData.DataValue<?>> packedItems;
		protected final CustomPacketPayload.Type<SynchedDataPacket> type;
		
		public SynchedDataPacket(int entityId, List<DataValue<?>> packedItems,
				SynchedDataPacketHandlerTemplate handler) {
			this.entityId = entityId;
			this.packedItems = packedItems;
			this.type = handler.type;
		}
		
		@Override
		public Type<? extends CustomPacketPayload> type() {
			return type;
		}
	}
	
}
