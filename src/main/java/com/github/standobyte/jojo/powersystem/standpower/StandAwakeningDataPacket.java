package com.github.standobyte.jojo.powersystem.standpower;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.powersystem.standpower.StandAwakening.AwakeningStage;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StandAwakeningDataPacket implements CustomPacketPayload {
	protected AwakeningStage stage;
	protected boolean hadStandBefore;
	protected Optional<Collection<ResourceLocation>> fatedFutureStands;
	protected Optional<Collection<ResourceLocation>> characterCanonStand;
	
	public StandAwakeningDataPacket(StandAwakening data) {
		this(data.stage, data.hadStandBefore, 
				Optional.ofNullable(data.fatedFutureStands), 
				Optional.ofNullable(data.characterCanonStand));
	}
	
	public StandAwakeningDataPacket(AwakeningStage stage, boolean hadStandBefore,
			Optional<Collection<ResourceLocation>> fatedFutureStands, 
			Optional<Collection<ResourceLocation>> characterCanonStand) {
		this.stage = stage;
		this.hadStandBefore = hadStandBefore;
		this.fatedFutureStands = fatedFutureStands;
		this.characterCanonStand = characterCanonStand;
	}
	
	
	private static CustomPacketPayload.Type<StandAwakeningDataPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<StandAwakeningDataPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<StandAwakeningDataPacket> type() {
			return type;
		}

		public static final StreamCodec<FriendlyByteBuf, Optional<Collection<ResourceLocation>>> OPTIONAL_COLLECTION = 
				NetworkUtil.collectionCodec(ResourceLocation.STREAM_CODEC).apply(ByteBufCodecs::optional);
		@Override
		public void encode(StandAwakeningDataPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeEnum(packet.stage);
			buf.writeBoolean(packet.hadStandBefore);
			OPTIONAL_COLLECTION.encode(buf, packet.fatedFutureStands);
			OPTIONAL_COLLECTION.encode(buf, packet.characterCanonStand);
		}

		@Override
		public StandAwakeningDataPacket decode(RegistryFriendlyByteBuf buf) {
			return new StandAwakeningDataPacket(
					buf.readEnum(AwakeningStage.class),
					buf.readBoolean(),
					OPTIONAL_COLLECTION.decode(buf),
					OPTIONAL_COLLECTION.decode(buf));
		}

		@Override
		public void handle(StandAwakeningDataPacket payload, IPayloadContext context) {
			Player player = ClientProxy.getClientPlayer();
			if (player != null) {
				StandPower stand = StandPower.get(player);
				if (stand != null) {
					stand.userStandAwakeningState.stage = payload.stage;
					stand.userStandAwakeningState.hadStandBefore = payload.hadStandBefore;
					stand.userStandAwakeningState.fatedFutureStands = payload.fatedFutureStands.map(HashSet::new).orElse(null);
					stand.userStandAwakeningState.characterCanonStand = payload.characterCanonStand.map(HashSet::new).orElse(null);
				}
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
