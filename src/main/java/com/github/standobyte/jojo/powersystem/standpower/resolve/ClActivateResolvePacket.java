package com.github.standobyte.jojo.powersystem.standpower.resolve;

import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClActivateResolvePacket(boolean activate) implements CustomPacketPayload {
	private static final ClActivateResolvePacket TRUE = new ClActivateResolvePacket(true);
	private static final ClActivateResolvePacket FALSE = new ClActivateResolvePacket(false);

	public static ClActivateResolvePacket of(boolean activate) {
		return activate ? TRUE : FALSE;
	}

	private static CustomPacketPayload.Type<ClActivateResolvePacket> type;

	public static class Handler implements PacketsRegister.PacketCodecHandler<ClActivateResolvePacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<ClActivateResolvePacket> type() {
			return type;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ClActivateResolvePacket> reader() {
			return STREAM_CODEC;
		}


		public static final StreamCodec<ByteBuf, ClActivateResolvePacket> STREAM_CODEC = ByteBufCodecs.BOOL
				.map(ClActivateResolvePacket::of, ClActivateResolvePacket::activate);

		@Override
		public void handle(ClActivateResolvePacket payload, IPayloadContext context) {
			Player player = context.player();
			StandPower standPower = StandPower.get(player);
			if (standPower.usesResolve()) {
				if (payload.activate) {
					standPower.resolveCounter.startResolveMode(standPower);
				}
				else {
					
				}
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
}
