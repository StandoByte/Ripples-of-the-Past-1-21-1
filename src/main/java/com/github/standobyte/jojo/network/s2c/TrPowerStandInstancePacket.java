package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrPowerStandInstancePacket implements CustomPacketPayload {
	private final int entityId;
	private final Optional<StandInstance.NetworkData> standInstance;
	
	public TrPowerStandInstancePacket(int entityId, Optional<StandInstance> standInstance) {
		this(standInstance.map(StandInstance.NetworkData::wrap), entityId);
	}
	
	// type erasure moment
	private TrPowerStandInstancePacket(Optional<StandInstance.NetworkData> standInstance, int entityId) {
		this.entityId = entityId;
		this.standInstance = standInstance;
	}
	
	
	private static CustomPacketPayload.Type<TrPowerStandInstancePacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<TrPowerStandInstancePacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrPowerStandInstancePacket> type() {
			return type;
		}

		public static final StreamCodec<FriendlyByteBuf, Optional<StandInstance.NetworkData>> STAND_INSTANCE_OPTIONAL_CODEC = 
				StandInstance.NetworkData.NETWORK_CODEC.apply(ByteBufCodecs::optional);
		@Override
		public void encode(TrPowerStandInstancePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.entityId);
			STAND_INSTANCE_OPTIONAL_CODEC.encode(buf, packet.standInstance);
		}

		@Override
		public TrPowerStandInstancePacket decode(RegistryFriendlyByteBuf buf) {
			int entityId = buf.readInt();
			Optional<StandInstance.NetworkData> standInstance = STAND_INSTANCE_OPTIONAL_CODEC.decode(buf);
			TrPowerStandInstancePacket packet = new TrPowerStandInstancePacket(
					standInstance, entityId);
			return packet;
		}

		@Override
		public void handle(TrPowerStandInstancePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.entityId);
			if (entity instanceof LivingEntity living) {
				StandPower standPower = StandPower.get(living);
				if (standPower != null) {
					standPower.setStandInstance(payload.standInstance.map(StandInstance.NetworkData::get));
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
