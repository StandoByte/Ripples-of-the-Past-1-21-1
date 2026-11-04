package com.github.standobyte.jojo.mechanics.explorer_map_creation.packets;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.mechanics.explorer_map_creation.CartographyExplorerMapContainerMenu;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClExplorerMapDataPacket implements CustomPacketPayload {
	private final FieldType packetType;
	private final int number;
	private final Holder<MapDecorationType> decorationType;
	private final String name;
	
	public static ClExplorerMapDataPacket posX(int posX) {
		return new ClExplorerMapDataPacket(FieldType.X, posX, null, null);
	}
	
	public static ClExplorerMapDataPacket posZ(int posZ) {
		return new ClExplorerMapDataPacket(FieldType.Z, posZ, null, null);
	}
	
	public static ClExplorerMapDataPacket destinationIcon(Holder<MapDecorationType> decorationType) {
		return new ClExplorerMapDataPacket(FieldType.DESTINATION_ICON, 0, decorationType, null);
	}
	
	public static ClExplorerMapDataPacket color(int color) {
		return new ClExplorerMapDataPacket(FieldType.COLOR, color, null, null);
	}
	
	public static ClExplorerMapDataPacket name(String name) {
		return new ClExplorerMapDataPacket(FieldType.NAME, 0, null, name);
	}
	
	private ClExplorerMapDataPacket(FieldType type, int number, 
			Holder<MapDecorationType> decorationType, String name) {
		this.packetType = type;
		this.number = number;
		this.decorationType = decorationType;
		this.name = name;
	}

	public enum FieldType {
		X,
		Z,
		DESTINATION_ICON,
		COLOR,
		NAME
	}
	
	
	
	private static CustomPacketPayload.Type<ClExplorerMapDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<ClExplorerMapDataPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClExplorerMapDataPacket> type() {
			return type;
		}

		@Override
		public void encode(ClExplorerMapDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case X, Z, COLOR -> buf.writeInt(packet.number);
				case DESTINATION_ICON -> MapDecorationType.STREAM_CODEC.encode(buf, packet.decorationType);
				case NAME -> buf.writeUtf(packet.name);
			}
		}

		@Override
		public ClExplorerMapDataPacket decode(RegistryFriendlyByteBuf buf) {
			FieldType packetType = buf.readEnum(FieldType.class);
			return switch (packetType) {
				case X, Z, COLOR -> new ClExplorerMapDataPacket(packetType, buf.readInt(), null, null);
				case DESTINATION_ICON -> new ClExplorerMapDataPacket(packetType, 0, MapDecorationType.STREAM_CODEC.decode(buf), null);
				case NAME -> new ClExplorerMapDataPacket(packetType, 0, null, buf.readUtf());
			};
		}

		@Override
		public void handle(ClExplorerMapDataPacket payload, IPayloadContext context) {
			ServerPlayer player = (ServerPlayer) context.player();
			if (player.containerMenu instanceof CartographyExplorerMapContainerMenu menu) {
				switch (payload.packetType) {
					case X -> {
						menu.setTargetPosX(payload.number);
					}
					case Z -> {
						menu.setTargetPosZ(payload.number);
					}
					case DESTINATION_ICON -> {
						menu.setTargetIcon(payload.decorationType);
					}
					case COLOR -> {
						menu.setMapItemColor(payload.number);
					}
					case NAME -> {
						menu.setMapItemName(payload.name);
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
