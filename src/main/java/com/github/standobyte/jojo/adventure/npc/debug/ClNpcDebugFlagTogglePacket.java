package com.github.standobyte.jojo.adventure.npc.debug;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClNpcDebugFlagTogglePacket implements CustomPacketPayload {
	private final PacketType packetType;
	private final int entityId;
	private final NpcFlags flag;
	private final boolean flagValue;
	private final String newName;
	
	public static ClNpcDebugFlagTogglePacket flag(int entityId, NpcFlags flag, boolean value) {
		return new ClNpcDebugFlagTogglePacket(PacketType.FLAG, entityId, flag, value, null);
	}
	
	public static ClNpcDebugFlagTogglePacket name(int entityId, String name) {
		return new ClNpcDebugFlagTogglePacket(PacketType.NAME, entityId, null, false, name);
	}
	
	public static ClNpcDebugFlagTogglePacket skin(int entityId, String playerName) {
		return new ClNpcDebugFlagTogglePacket(PacketType.SKIN, entityId, null, false, playerName);
	}
	
	private ClNpcDebugFlagTogglePacket(PacketType packetType, int entityId, 
			NpcFlags flag, boolean flagValue, String newName) {
		this.packetType = packetType;
		this.entityId = entityId;
		this.flag = flag;
		this.flagValue = flagValue;
		this.newName = newName;
	}
	
	public enum PacketType {
		FLAG,
		NAME,
		SKIN
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
			buf.writeEnum(packet.packetType);
			buf.writeInt(packet.entityId);
			switch (packet.packetType) {
				case FLAG -> {
					buf.writeEnum(packet.flag);
					buf.writeBoolean(packet.flagValue);
				}
				case NAME, SKIN -> {
					buf.writeUtf(packet.newName);
				}
			}
		}

		@Override
		public ClNpcDebugFlagTogglePacket decode(RegistryFriendlyByteBuf buf) {
			PacketType packetType = buf.readEnum(PacketType.class);
			int entityId = buf.readInt();
			return switch (packetType) {
				case FLAG -> {
					NpcFlags flag = buf.readEnum(NpcFlags.class);
					boolean value = buf.readBoolean();
					yield ClNpcDebugFlagTogglePacket.flag(entityId, flag, value);
				}
				case NAME, SKIN -> {
					String name = buf.readUtf();
					yield new ClNpcDebugFlagTogglePacket(packetType, entityId, null, false, name);
				}
			};
		}

		@Override
		public void handle(ClNpcDebugFlagTogglePacket payload, IPayloadContext context) {
			Player player = context.player();
			if (player.getInventory().contains(item -> item.is(ModItems.CHARACTER_TEST.get()))
					&& player.level().getEntity(payload.entityId) instanceof PowerUserMobEntity npc) {
				switch (payload.packetType) {
					case FLAG -> {
						npc.setFlag(payload.flag, payload.flagValue);
					}
					case NAME -> {
						npc.setCustomName(Component.literal(payload.newName));
					}
					case SKIN -> {
						npc.getEntityData().set(PowerUserMobEntity.DATA_PROFILE, Optional.of(
								new ResolvableProfile(Optional.of(payload.newName), Optional.empty(), new PropertyMap())));
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
