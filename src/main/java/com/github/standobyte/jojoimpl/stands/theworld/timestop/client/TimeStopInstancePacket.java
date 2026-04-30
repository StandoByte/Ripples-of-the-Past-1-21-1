package com.github.standobyte.jojoimpl.stands.theworld.timestop.client;

import java.util.Collection;
import java.util.Collections;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopEffect;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopClientLevelInstance;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopClientLevelTracker;

import it.unimi.dsi.fastutil.ints.IntCollection;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TimeStopInstancePacket implements CustomPacketPayload {
	public final PacketType packetType;
	public final Collection<TimeStopEffect> addWrite;
	public final Collection<TimeStopClientLevelInstance> addRead;
	public final int[] remove;
	public final ResourceKey<Level> joiningDimension;
	
	protected TimeStopInstancePacket(PacketType packetType, Collection<TimeStopEffect> addWrite, 
			Collection<TimeStopClientLevelInstance> addRead, int[] remove, ResourceKey<Level> joiningDimension) {
		this.packetType = packetType;
		this.addWrite = addWrite;
		this.addRead = addRead;
		this.remove = remove;
		this.joiningDimension = joiningDimension;
	}

	public static TimeStopInstancePacket add(TimeStopEffect effect) {
		return new TimeStopInstancePacket(PacketType.ADD, Collections.singletonList(effect), null, null, null);
	}
	
	public static TimeStopInstancePacket remove(int effectId) {
		return new TimeStopInstancePacket(PacketType.REMOVE, null, null, new int[] { effectId }, null);
	}
	
	public static TimeStopInstancePacket removeMultiple(IntCollection effectIds) {
		return new TimeStopInstancePacket(PacketType.REMOVE, null, null, effectIds.toIntArray(), null);
	}
	
	public static TimeStopInstancePacket allOnLevelJoin(ResourceKey<Level> joiningDimension, Collection<TimeStopEffect> allEffects) {
		return new TimeStopInstancePacket(PacketType.SYNC_ON_JOIN_LEVEL, allEffects, null, null, joiningDimension);
	}
	
	enum PacketType {
		ADD,
		REMOVE,
		SYNC_ON_JOIN_LEVEL
	}

	
	
	private static CustomPacketPayload.Type<TimeStopInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TimeStopInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TimeStopInstancePacket> type() {
			return type;
		}

		@Override
		public void encode(TimeStopInstancePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.packetType);
			switch (packet.packetType) {
				case ADD -> {
					NetworkUtil.writeCollection(buf, packet.addWrite, TimeStopClientLevelInstance::toBuf);
				}
				case REMOVE -> {
					buf.writeVarInt(packet.remove.length);
					for (int i : packet.remove) {
						buf.writeInt(i);
					}
				}
				case SYNC_ON_JOIN_LEVEL -> {
					NetworkUtil.writeCollection(buf, packet.addWrite, TimeStopClientLevelInstance::toBuf);
					buf.writeResourceKey(packet.joiningDimension);
				}
			}
		}

		@Override
		public TimeStopInstancePacket decode(RegistryFriendlyByteBuf buf) {
			PacketType packetType = buf.readEnum(PacketType.class);
			return switch (packetType) {
				case ADD -> {
					Collection<TimeStopClientLevelInstance> instances = NetworkUtil.readCollection(buf, TimeStopClientLevelInstance::fromBuf);
					yield new TimeStopInstancePacket(packetType, null, instances, null, null);
				}
				case REMOVE -> {
					int n = buf.readVarInt();
					int[] remove = new int[n];
					for (int i = 0; i < n; i++) {
						remove[i] = buf.readInt();
					}
					yield new TimeStopInstancePacket(packetType, null, null, remove, null);
				}
				case SYNC_ON_JOIN_LEVEL -> {
					Collection<TimeStopClientLevelInstance> instances = NetworkUtil.readCollection(buf, TimeStopClientLevelInstance::fromBuf);
					ResourceKey<Level> joiningDimension = buf.readResourceKey(Registries.DIMENSION);
					yield new TimeStopInstancePacket(packetType, null, instances, null, joiningDimension);
				}
			};
		}

		@Override
		public void handle(TimeStopInstancePacket payload, IPayloadContext context) {
			TimeStopClientLevelTracker tracker = TimeStopClientState.levelTimeStops;
			switch (payload.packetType) {
				case ADD -> {
					for (var instance : payload.addRead) {
						tracker.activeEffects.put(instance.id(), instance);
					}
				}
				case REMOVE -> {
					for (int id : payload.remove) {
						tracker.activeEffects.remove(id);
					}
				}
				case SYNC_ON_JOIN_LEVEL -> {
					tracker.dimension = payload.joiningDimension;
					tracker.activeEffects.clear();
					for (var instance : payload.addRead) {
						tracker.activeEffects.put(instance.id(), instance);
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
