package com.github.standobyte.jojo.adventure.npc.debug;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.init.ModItems;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClNpcDebugFlagTogglePacket implements CustomPacketPayload {
	private final int entityId;
	private final NpcFlags flag;
	private final boolean value;
	
	public ClNpcDebugFlagTogglePacket(int entityId, NpcFlags flag, boolean value) {
		this.entityId = entityId;
		this.flag = flag;
		this.value = value;
	}
	
	
	
	private static CustomPacketPayload.Type<ClNpcDebugFlagTogglePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClNpcDebugFlagTogglePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClNpcDebugFlagTogglePacket> type() {
			return type;
		}

		@Override
		public void encode(ClNpcDebugFlagTogglePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			buf.writeEnum(packet.flag);
			buf.writeBoolean(packet.value);
		}

		@Override
		public ClNpcDebugFlagTogglePacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			NpcFlags flag = buf.readEnum(NpcFlags.class);
			boolean value = buf.readBoolean();
			return new ClNpcDebugFlagTogglePacket(entityId, flag, value);
		}

		@Override
		public void handle(ClNpcDebugFlagTogglePacket payload, IPayloadContext context) {
			Player player = context.player();
			if (player.getMainHandItem().getItem() == ModItems.CHARACTER_TEST.get()
					&& player.level().getEntity(payload.entityId) instanceof PowerUserMobEntity npc) {
				npc.setFlag(payload.flag, payload.value);
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
