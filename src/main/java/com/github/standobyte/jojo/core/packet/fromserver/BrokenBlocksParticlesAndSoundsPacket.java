package com.github.standobyte.jojo.core.packet.fromserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.core.PacketsRegister;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.network.NetworkUtil;
import com.github.standobyte.jojo.util.network.PacketDistributor2;
import com.google.common.collect.Streams;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class BrokenBlocksParticlesAndSoundsPacket implements CustomPacketPayload {
	public List<BrokenBlock> brokenBlocks;

	public BrokenBlocksParticlesAndSoundsPacket() {
		this(new ArrayList<>());
	}

	private BrokenBlocksParticlesAndSoundsPacket(List<BrokenBlock> brokenBlocks) {
		this.brokenBlocks = brokenBlocks;
	}

	public void addBlock(BlockPos blockPos, BlockState blockState) {
		brokenBlocks.add(new BrokenBlock(blockPos, blockState));
	}

	public void sendToPlayers(ServerLevel level, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		if (brokenBlocks.isEmpty()) return;

		brokenBlocks = limitRandom(brokenBlocks, 256);

		final double radius = 64;
		Packet<?> vanillaPacket = PacketDistributor2.makeClientboundPacket(this);
		for (ServerPlayer player : level.players()) {
			if (player.level().dimension() == level.dimension()) {
				double x = player.getX();
				double y = player.getY();
				double z = player.getZ();

				double xDiff = x < minX ? minX - x : x > maxX ? x - maxX : 0;
				double yDiff = y < minY ? minY - y : y > maxY ? y - maxY : 0;
				double zDiff = z < minZ ? minZ - z : z > maxZ ? z - maxZ : 0;
				if (xDiff * xDiff + yDiff * yDiff + zDiff * zDiff < radius * radius) {
					player.connection.send(vanillaPacket);
				}
			}
		}
	}

	public static <E> List<E> limitRandom(List<E> randomAccessMutableList, int limit) {
		int size = randomAccessMutableList.size();
		if (size > limit) {
			for (int i = 0; i < limit; i++){
				int index = i + OOPMoment.RANDOM.nextInt(size - i);
				E tmp = randomAccessMutableList.get(index);
				randomAccessMutableList.set(index, randomAccessMutableList.get(i));
				randomAccessMutableList.set(i, tmp);
			}
			return randomAccessMutableList.stream().limit(limit).collect(Collectors.toList());
		}
		return randomAccessMutableList;
	}


	public static class BrokenBlock {
		public final BlockPos blockPos;
		protected final int blockStateData;
		public BlockState blockState;

		protected BrokenBlock(BlockPos blockPos, int data) {
			this.blockPos = blockPos;
			this.blockStateData = data;
		}

		public BrokenBlock(BlockPos blockPos, BlockState blockState) {
			this(blockPos, Block.getId(blockState));
			this.blockState = blockState;
		}
		
		public static final StreamCodec<ByteBuf, BrokenBlock> STREAM_CODEC = new StreamCodec<>() {

			@Override
			public BrokenBlock decode(ByteBuf buffer) {
				BlockPos blockPos = BlockPos.STREAM_CODEC.decode(buffer);
				int data = buffer.readInt();
				return new BrokenBlock(blockPos, data);
			}

			@Override
			public void encode(ByteBuf buffer, BrokenBlock value) {
				BlockPos.STREAM_CODEC.encode(buffer, value.blockPos);
				buffer.writeInt(value.blockStateData);
			}
			
		};

		public void handleResolveBlockState() {
			this.blockState = Block.stateById(blockStateData);
		}
	}



	private static CustomPacketPayload.Type<BrokenBlocksParticlesAndSoundsPacket> type;

	public static class Handler implements PacketsRegister.PacketOGHandler<BrokenBlocksParticlesAndSoundsPacket> {

		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<BrokenBlocksParticlesAndSoundsPacket> type() {
			return type;
		}

		@Override
		public void encode(BrokenBlocksParticlesAndSoundsPacket packet, RegistryFriendlyByteBuf buf) {
			NetworkUtil.writeCollection(buf, packet.brokenBlocks, BrokenBlock.STREAM_CODEC);
		}

		@Override
		public BrokenBlocksParticlesAndSoundsPacket decode(RegistryFriendlyByteBuf buf) {
			return new BrokenBlocksParticlesAndSoundsPacket(NetworkUtil.readCollection(buf, BrokenBlock.STREAM_CODEC));
		}

		@Override
		public void handle(BrokenBlocksParticlesAndSoundsPacket payload, IPayloadContext context) {
			Stream<BrokenBlock> stream = payload.brokenBlocks.stream();
			if (payload.brokenBlocks.size() > 128) {
				Vec3 cameraPos = ClientProxy.getCameraPos();
				stream = stream
						.sorted(Comparator.comparingDouble(block -> block.blockPos.distToCenterSqr(cameraPos.x, cameraPos.y, cameraPos.z)))
						.limit(128);
			}
			Streams.mapWithIndex(stream, (block, index) -> {
				block.handleResolveBlockState();
				
				Level world = ClientProxy.getClientWorld();
				if (!block.blockState.isAir()) {
					int particlesSetting = CustomParticlesHelper.particlesSetting();
					if (particlesSetting < 2 && (particlesSetting < 1 || index % 2 == 0)) {
						CustomParticlesHelper.addBlockBreakParticles(block.blockPos, block.blockState);
					}
					SoundType soundType = block.blockState.getSoundType(world, block.blockPos, null);
					if (index % 8 == 0) {
						world.playLocalSound(
								block.blockPos.getX() + 0.5, 
								block.blockPos.getY() + 0.5, 
								block.blockPos.getZ() + 0.5, 
								soundType.getBreakSound(), SoundSource.BLOCKS, 
								(soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
					}
				}
				
				return block;
			}).forEach(block -> {});
		}

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}

}
