package com.github.standobyte.jojo.mechanics.resolve;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TrResolvePacket implements CustomPacketPayload {
	private int userId;
	private boolean toPlayerUser;
	private ResolveCounter variablesToWrite;
	private FriendlyByteBuf multipliersReadData;

	public TrResolvePacket(int userId, boolean toPlayerUser, ResolveCounter resolve) {
		this.userId = userId;
		this.toPlayerUser = toPlayerUser;
		this.variablesToWrite = resolve;
	}
	
	private TrResolvePacket(int userId, boolean toPlayerUser, FriendlyByteBuf multipliersReadData) {
		this.userId = userId;
		this.toPlayerUser = toPlayerUser;
		this.multipliersReadData = multipliersReadData;
	}
	
	
	
	private static CustomPacketPayload.Type<TrResolvePacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<TrResolvePacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<TrResolvePacket> type() {
			return type;
		}

		@Override
		public void encode(TrResolvePacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeInt(packet.userId);
			buf.writeBoolean(packet.toPlayerUser);
			packet.variablesToWrite.toBuf(buf, packet.toPlayerUser);
		}

		@Override
		public TrResolvePacket decode(RegistryFriendlyByteBuf buf) {
			int userId = buf.readInt();
			boolean toPlayerUser = buf.readBoolean();
			FriendlyByteBuf multipliersData = NetworkUtil.extraPacketData(buf);
			return new TrResolvePacket(userId, toPlayerUser, multipliersData);
		}

		@Override
		public void handle(TrResolvePacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.userId);
			if (entity instanceof LivingEntity living) {
				ResolveCounter resolveCounter = ResolveCounter.getOrCreate(living);
				if (resolveCounter != null) {
					resolveCounter.fromBuf(payload.multipliersReadData, payload.toPlayerUser);
				}
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
