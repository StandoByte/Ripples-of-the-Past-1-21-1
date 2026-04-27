package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import java.util.stream.IntStream;

import com.github.standobyte.jojo.JojoModEntityVariables;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.network.s2c.EntityDirectPosNoLerpPacket;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

public class TimeStopEffect extends StandEffectInstance {
	public int duration = 100;
	public ChunkPos initialPos;

	public TimeStopEffect(EntityCustomEffectType<?> effectType) {
		super(effectType);
	}
	

	@Override
	protected void start() {
		if (this.level != null) {
			TimeStopLevelTracker levelTracker = level.getData(ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
			levelTracker.add(this);
		}
	}

	@Override
	protected void tick() {
		if (!level.isClientSide() && this.tickCount >= duration) {
			this.remove();
		}
	}

	@Override
	protected void stop() {
		if (this.level != null) {
			TimeStopLevelTracker levelTracker = level.getData(ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
			levelTracker.remove(this);
		}
	}

	
	public static final int CHUNK_RANGE = 12;
	public boolean isInRange(BlockPos blockPos) {
		return isInRange(initialPos.x, initialPos.z, 
				SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()), 
				CHUNK_RANGE);
	}
	
	public boolean isInRange(ChunkPos chunkPos) {
		return isInRange(initialPos.x, initialPos.z, 
				chunkPos.x, chunkPos.z, 
				CHUNK_RANGE);
	}
	
	public static boolean isInRange(int x1, int z1, int x2, int z2, int range) {
		return range <= 0 || Math.abs(x1 - x2) < range && Math.abs(z1 - z2) < range;
	}



	// copypasted from the BlockPos codec
	public static final Codec<ChunkPos> FUCK_MY_LIFE = Codec.INT_STREAM.<ChunkPos>comapFlatMap(
			intStream -> Util.fixedSize(intStream, 2).map(arr -> new ChunkPos(arr[0], arr[1])),
			chunkPos -> IntStream.of(chunkPos.x, chunkPos.z))
			.stable() /* unlike my mental state */;
		
	@Override
	protected void writeAdditionalSaveData(CompoundTag nbt) {
		super.writeAdditionalSaveData(nbt);
		nbt.putInt("Duration", duration);
		NBTUtil.put(nbt, "Pos", initialPos, FUCK_MY_LIFE);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		duration = nbt.getInt("Duration");
		initialPos = NBTUtil.getOptional(nbt, "Pos", FUCK_MY_LIFE).orElseThrow();
	}
	
	
	public static void setTimeStopState(Entity entity, boolean state) {
		if (state) {
			var variables = JojoModEntityVariables.get(entity);
			variables.synchedData.set(JojoModEntityVariables.STOPPED_IN_TIME, true);
			// call this manually - because the tick will be cancelled, this method won't be called while the entity is frozen
			variables.tickSyncDirtyData();
			// should prevent the old position desync if the entity was moving at high speed
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EntityDirectPosNoLerpPacket(entity.getId(), entity.position()));
		}
		else {
			var variables = JojoModEntityVariables.getIfPresent(entity);
			if (variables != null) {
				variables.synchedData.set(JojoModEntityVariables.STOPPED_IN_TIME, false);
			}
		}
	}
	
	public static boolean getTimeStopState(Entity entity) {
		var variables = JojoModEntityVariables.getIfPresent(entity);
		return variables != null ? variables.synchedData.get(JojoModEntityVariables.STOPPED_IN_TIME) : false;
	}

}
