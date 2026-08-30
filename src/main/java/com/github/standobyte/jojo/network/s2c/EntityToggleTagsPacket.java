package com.github.standobyte.jojo.network.s2c;

import java.util.Collection;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.entityattachment.ToggleTags;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EntityToggleTagsPacket implements CustomPacketPayload {
	private final int entityId;
	private final Collection<String> tags;
	
	public EntityToggleTagsPacket(int entityId, Collection<String> tags) {
		this.entityId = entityId;
		this.tags = tags;
	}



	private static CustomPacketPayload.Type<EntityToggleTagsPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<EntityToggleTagsPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<EntityToggleTagsPacket> type() {
			return type;
		}
		
		@Override
		public void encode(EntityToggleTagsPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			NetworkUtil.writeCollection(buf, packet.tags, ByteBufCodecs.STRING_UTF8);
		}
		
		@Override
		public EntityToggleTagsPacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			Collection<String> tags = NetworkUtil.readCollection(buf, ByteBufCodecs.STRING_UTF8);
			return new EntityToggleTagsPacket(entityId, tags);
		}

		@Override
		public void handle(EntityToggleTagsPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			ToggleTags tags = ToggleTags.get(entity);
			for (String tag : payload.tags) {
				tags.addTag(tag, false, false);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
