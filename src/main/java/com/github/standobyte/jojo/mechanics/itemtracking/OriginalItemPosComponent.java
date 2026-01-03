package com.github.standobyte.jojo.mechanics.itemtracking;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

public record OriginalItemPosComponent(BlockPos blockPos) {
	public static final Codec<OriginalItemPosComponent> CODEC = BlockPos.CODEC.xmap(
			OriginalItemPosComponent::new, OriginalItemPosComponent::blockPos);
	
	public static final StreamCodec<ByteBuf, OriginalItemPosComponent> STREAM_CODEC = BlockPos.STREAM_CODEC.map(
			OriginalItemPosComponent::new, OriginalItemPosComponent::blockPos);

}
