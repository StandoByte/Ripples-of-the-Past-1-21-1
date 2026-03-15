package com.github.standobyte.jojo.powersystem.entityaction.syncdata;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrActionSynchedDataPacket(int entityId, List<SynchedEntityData.DataValue<?>> packedItems) implements CustomPacketPayload {
	private static CustomPacketPayload.Type<TrActionSynchedDataPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrActionSynchedDataPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrActionSynchedDataPacket> type() {
			return type;
		}

		@Override
		public void encode(TrActionSynchedDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);

			for (SynchedEntityData.DataValue<?> datavalue : packet.packedItems) {
				datavalue.write(buf);
			}
			buf.writeByte(255);
		}

		@Override
		public TrActionSynchedDataPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();

			List<SynchedEntityData.DataValue<?>> packedItems = new ArrayList<>();
			int id;
			while ((id = buf.readUnsignedByte()) != 255) {
				packedItems.add(SynchedEntityData.DataValue.read(buf, id));
			}

			return new TrActionSynchedDataPacket(entityId, packedItems);
		}

		@Override
		public void handle(TrActionSynchedDataPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				SyncActionInstanceData.setDataClientSide(living, payload.packedItems);
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
