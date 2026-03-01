package com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BrokenChunkBlocksPacket(Collection<PrevBlockInfo> blocks, boolean reset) implements CustomPacketPayload {

	private static CustomPacketPayload.Type<BrokenChunkBlocksPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<BrokenChunkBlocksPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<BrokenChunkBlocksPacket> type() {
			return type;
		}

		@Override
		public void encode(BrokenChunkBlocksPacket packet, RegistryFriendlyByteBuf buf) {
			NetworkUtil.writeCollection(buf, packet.blocks, PrevBlockInfo.STREAM_CODEC);
			buf.writeBoolean(packet.reset);
		}

		@Override
		public BrokenChunkBlocksPacket decode(RegistryFriendlyByteBuf buf) {
			Collection<PrevBlockInfo> blocks = NetworkUtil.readCollection(buf, PrevBlockInfo.STREAM_CODEC);
			boolean reset = buf.readBoolean();
			return new BrokenChunkBlocksPacket(blocks, reset);
		}

		@Override
		public void handle(BrokenChunkBlocksPacket payload, IPayloadContext context) {
			Level world = ClientProxy.getClientWorld();
			for (PrevBlockInfo block : payload.blocks) {
				BrokenBlocksChunkData data = BrokenBlocksChunkData.getChunkData(world, block.pos);
				if (data != null) {
					if (payload.reset) {
						data.reset();
					}
					if (block.state != Blocks.AIR.defaultBlockState()) {
						data.saveBrokenBlock(block);
					}
					else {
						data.removeBrokenBlock(block.pos);
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
