package com.github.standobyte.jojo.adventure.character;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrCharacterDataPacket implements CustomPacketPayload {
	private final int entityId;
	private CharacterPersonData serverData;
	private RegistryFriendlyByteBuf clientRead;
	
	public TrCharacterDataPacket(int entityId, CharacterPersonData serverData) {
		this.entityId = entityId;
		this.serverData = serverData;
	}
	
	private TrCharacterDataPacket(int entityId, RegistryFriendlyByteBuf clientRead) {
		this.entityId = entityId;
		this.clientRead = clientRead;
	}
	
	
	private static CustomPacketPayload.Type<TrCharacterDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrCharacterDataPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrCharacterDataPacket> type() {
			return type;
		}

		@Override
		public void encode(TrCharacterDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			packet.serverData.toBuf(buf);
		}

		@Override
		public TrCharacterDataPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			RegistryFriendlyByteBuf readData = NetworkUtil.extraPacketData(buf);
			return new TrCharacterDataPacket(entityId, readData);
		}

		@Override
		public void handle(TrCharacterDataPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				CharacterPersonData data = CharacterPersonData.get(living);
				if (data != null) {
					data.fromBuf(payload.clientRead);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
