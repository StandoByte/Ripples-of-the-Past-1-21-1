package com.github.standobyte.core_subsystems.entitydata.sync;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.core_subsystems.entitydata.EntityAttachType;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrStandEffectSynchedDataPacket(int entityId, int effectId, 
		EntityAttachType attachmentType, List<SynchedEntityData.DataValue<?>> packedItems) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrStandEffectSynchedDataPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrStandEffectSynchedDataPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrStandEffectSynchedDataPacket> type() {
			return type;
		}

		@Override
		public void encode(TrStandEffectSynchedDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeInt(packet.effectId);
			buf.writeEnum(packet.attachmentType);

			for (SynchedEntityData.DataValue<?> datavalue : packet.packedItems) {
				datavalue.write(buf);
			}
			buf.writeByte(255);
		}

		@Override
		public TrStandEffectSynchedDataPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			int effectId = buf.readInt();
			EntityAttachType attachmentType = buf.readEnum(EntityAttachType.class);

			List<SynchedEntityData.DataValue<?>> packedItems = new ArrayList<>();
			int id;
			while ((id = buf.readUnsignedByte()) != 255) {
				packedItems.add(SynchedEntityData.DataValue.read(buf, id));
			}

			return new TrStandEffectSynchedDataPacket(entityId, effectId, attachmentType, packedItems);
		}

		@Override
		public void handle(TrStandEffectSynchedDataPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			SyncStandEffectInstanceData.setDataClientSide(entity, payload.effectId, payload.attachmentType, payload.packedItems);
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
