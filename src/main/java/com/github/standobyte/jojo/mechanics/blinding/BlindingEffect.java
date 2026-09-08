package com.github.standobyte.jojo.mechanics.blinding;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffect;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.init.ModEntityCustomEffects;
import com.github.standobyte.jojo.util.OOPMoment;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.functions_network.StreamCodecs;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class BlindingEffect extends EntityCustomEffect {
	public BlindingParticlesType type = BlindingParticlesType.DEFAULT;
	public int lifeSpan = 80;
	public float ratio = 1;
	public int clientSpritesSeed;
	public ParticlesRNGCache clientSprites;
	
	@Nullable public BlockState block;
	@Nullable public BlockPos blockPos;
	public int blockTint = -1;

	public BlindingEffect() {
		this(ModEntityCustomEffects.BLINDING_BLOOD_OR_SAND.get());
	}

	public BlindingEffect(EntityCustomEffectType<?> effectType) {
		super(effectType);
	}
	
	
	public enum BlindingParticlesType {
		BLOOD,
		SAND;
		
		public static final BlindingParticlesType DEFAULT = BlindingParticlesType.SAND;
	}
	
	public void initType(BlindingParticlesType type) {
		this.type = type;
		this.clientSpritesSeed = OOPMoment.RANDOM.nextInt();
	}
	

	@Override
	protected void start() {
	}

	@Override
	protected void tick() {
		if (!level.isClientSide() && tickCount >= lifeSpan) {
			remove();
		}
	}

	@Override
	protected void stop() {
	}
	
	@Override
	public void writeAdditionalPacketData(FriendlyByteBuf buf, boolean sendingToUser) {
		if (type == null) type = BlindingParticlesType.DEFAULT;
		buf.writeEnum(type);
		buf.writeFloat(ratio);
		buf.writeInt(clientSpritesSeed);
		NetworkUtil.writeOptionally(block, buf, StreamCodecs.DEFAULT_BLOCK_BLOCKSTATE);
		NetworkUtil.writeOptionally(blockPos, buf, BlockPos.STREAM_CODEC);
	}

	@Override
	public void readAdditionalPacketData(FriendlyByteBuf buf, boolean clientIsUser) {
		type = buf.readEnum(BlindingParticlesType.class);
		ratio = buf.readFloat();
		clientSpritesSeed = buf.readInt();
		block = NetworkUtil.readOptional(buf, StreamCodecs.DEFAULT_BLOCK_BLOCKSTATE).orElse(null);
		blockPos = NetworkUtil.readOptional(buf, BlockPos.STREAM_CODEC).orElse(null);
	}

	@Override
	protected void writeAdditionalSaveData(CompoundTag nbt, HolderLookup.Provider registries) {
		if (type == null) type = BlindingParticlesType.DEFAULT;
		NBTUtil.putEnum(nbt, "type", type);
		nbt.putFloat("ratio", ratio);
		nbt.putInt("seed", clientSpritesSeed);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt, HolderLookup.Provider registries) {
		type = NBTUtil.getEnum(nbt, "type", BlindingParticlesType.class);
		ratio = NBTUtil.getOptional(nbt, "ratio", Codec.FLOAT).orElse(1f);
		clientSpritesSeed = nbt.getInt("seed");
	}
	
	
	public static class ParticlesRNGCache {
		public final int spriteCount;
		
		public final float[] timeOffset;
		public final int[] spriteIndex;
		public final float[] dripDownSpeed;
		public final float[] x;
		public final float[] y;
		public final float[] scaleMult;
		public final float[] tintMult;
		
		public ParticlesRNGCache(int spriteCount, float[] timeOffset, int[] spriteIndex, float[] dripDownSpeed, 
				float[] x, float[] y, float[] scaleMult, float[] tintMult) {
			this.spriteCount = spriteCount;
			this.timeOffset = timeOffset;
			this.spriteIndex = spriteIndex;
			this.dripDownSpeed = dripDownSpeed;
			this.x = x;
			this.y = y;
			this.scaleMult = scaleMult;
			this.tintMult = tintMult;
		}
	}

}
