package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.brokenblocks;

import java.util.Collection;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CDBlocksRestoredPacket(Collection<BlockPos> positions) implements CustomPacketPayload {

	private static CustomPacketPayload.Type<CDBlocksRestoredPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<CDBlocksRestoredPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<CDBlocksRestoredPacket> type() {
			return type;
		}

		@Override
		public void encode(CDBlocksRestoredPacket packet, RegistryFriendlyByteBuf buf) {
			NetworkUtil.writeCollection(buf, packet.positions, BlockPos.STREAM_CODEC);
		}

		@Override
		public CDBlocksRestoredPacket decode(RegistryFriendlyByteBuf buf) {
			Collection<BlockPos> positions = NetworkUtil.readCollection(buf, BlockPos.STREAM_CODEC);
			return new CDBlocksRestoredPacket(positions);
		}

		@Override
		public void handle(CDBlocksRestoredPacket payload, IPayloadContext context) {
			// FIXME do not send these packets to non-stand users at all
			if (ClientGlobals.canSeeStands) {
				Level level = ClientProxy.getClientWorld();
				for (BlockPos pos : payload.positions) {
					CrazyDRestoreTerrainAbility.addParticlesAroundBlock(level, pos, level.getRandom());
				}
			}
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
