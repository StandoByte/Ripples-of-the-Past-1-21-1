package com.github.standobyte.jojo.network.s2c;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.SummonedStand;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrNonEntityStandSummonPacket implements CustomPacketPayload {
	private final int userId;
	private final boolean summoned;
	private SummonedStand serverStand;
	private RegistryFriendlyByteBuf clientReadData;
	
	@Deprecated
	public TrNonEntityStandSummonPacket(int userId, boolean summoned) {
		this(userId, summoned, (SummonedStand) null);
	}
	
	public TrNonEntityStandSummonPacket(int userId, boolean summoned, SummonedStand serverStand) {
		this.userId = userId;
		this.summoned = summoned;
		this.serverStand = serverStand;
	}
	
	private TrNonEntityStandSummonPacket(int userId, boolean summoned, RegistryFriendlyByteBuf clientReadData) {
		this.userId = userId;
		this.summoned = summoned;
		this.clientReadData = clientReadData;
	}



	private static CustomPacketPayload.Type<TrNonEntityStandSummonPacket> type;
	public static class Handler implements PacketsRegister.PacketOGHandler<TrNonEntityStandSummonPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrNonEntityStandSummonPacket> type() {
			return type;
		}

		@Override
		public void encode(TrNonEntityStandSummonPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.userId);
			buf.writeBoolean(packet.summoned);
			if (packet.serverStand != null) {
				packet.serverStand.nonEntityStandDataToBuf(buf);
			}
		}

		@Override
		public TrNonEntityStandSummonPacket decode(RegistryFriendlyByteBuf buf) {
			int userId = buf.readInt();
			boolean summoned = buf.readBoolean();
			RegistryFriendlyByteBuf readData = NetworkUtil.extraPacketData(buf);
			TrNonEntityStandSummonPacket packet = new TrNonEntityStandSummonPacket(userId, summoned, readData);
			return packet;
		}

		@Override
		public void handle(TrNonEntityStandSummonPacket payload, IPayloadContext context) {
			Entity userEntity = ClientProxy.getEntityById(payload.userId);
			if (userEntity instanceof LivingEntity userLiving) {
				StandPower standPower = StandPower.get(userLiving);
				if (standPower != null && standPower.hasPower()) {
					if (payload.summoned) {
						standPower.getPowerType().summon(userLiving, standPower);
						
						if (payload.clientReadData != null) {
							SummonedStand summonedStand = standPower.getSummonedStand();
							if (summonedStand != null) {
								summonedStand.nonEntityStandDataFromBuf(payload.clientReadData);
							}
						}
					}
					else {
						standPower.getPowerType().forceUnsummon(userLiving, standPower);
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
