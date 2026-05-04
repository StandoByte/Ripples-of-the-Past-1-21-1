package com.github.standobyte.jojoimpl.stands.theworld.timestop;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.standobyte.jojo.JojoModEntityVariables;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.power.ModStandAbilities;
import com.github.standobyte.jojo.network.s2c.EntityDirectPosNoLerpPacket;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.functions_network.PacketDistributor2;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.TimeStopVFXPacket.TimeStopVFXState;
import com.github.standobyte.jojoimpl.stands.theworld.timestop.level.TimeStopLevelTracker;
import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

// FIXME time stop in NBT
/*
 * - don't save this effect if the player logs out in multiplayer (only keep in singleplayer)
 * - when loading the effect from NBT in singleplayer, no entities have been loaded yet, 
 *   so TimeStopLevelTracker#add does nothing - defer the call somehow
 */
public class TimeStopEffect extends StandEffectInstance implements TimeStopInstance {
	public boolean playedFX = false;
	public int duration = 100;
	public ChunkPos initialPos;

	public TimeStopEffect(EntityCustomEffectType<?> effectType) {
		super(effectType);
	}
	
	@Override
	public void setEntity(Entity entity) {
		super.setEntity(entity);
		this.initialPos = entity.chunkPosition();
	}

	@Override public ChunkPos center() { return initialPos; }
	public static final int CHUNK_RANGE = 12;
	@Override public int chunkRange() { return CHUNK_RANGE; }
	

	@Override
	protected void start() {
		if (this.level != null && !level.isClientSide()) {
			TimeStopLevelTracker levelTracker = level.getData(ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
			levelTracker.add(this);

			TimeStopVFXPacket vfxPacket = shaderPacket(!playedFX ? TimeStopVFXState.STARTUP : TimeStopVFXState.ACTIVE);
			Stream<ServerPlayer> sendTo = ((ServerLevel) level).players().stream()
					.filter(player -> isInRange(player.blockPosition()) && getCanSeeInTimeStopVar(player));
			PacketDistributor2.sendToPlayers(level, sendTo, vfxPacket);

			if (!playedFX) {
				playedFX = true;
			}
		}
	}
	
	public TimeStopVFXPacket shaderPacket(TimeStopVFXState state) {
		LivingEntity user = getStandUser();
		StandPower userPower = getUserPower();
		Optional<StandInstance> stand = userPower != null ? userPower.getStandInstance() : Optional.empty();
		
		ResourceLocation standId = stand.map(StandInstance::getStandId).orElseGet(() -> JojoMod.resLoc("the_world"));
		Optional<ResourceLocation> selectedSkin = stand.flatMap(StandInstance::getSelectedSkin);
		Optional<Vec3> pos = user != null ? Optional.of(user.position()) : Optional.empty();
		int userId = user != null ? user.getId() : -1;
		
		return new TimeStopVFXPacket(standId, selectedSkin, pos, userId, state);
	}

	@Override
	protected void tick() {
		if (!level.isClientSide() && this.tickCount >= duration) {
			this.remove();
		}
	}

	@Override
	protected void stop() {
		if (this.level != null && !level.isClientSide()) {
			TimeStopLevelTracker levelTracker = level.getData(ModDataAttachmentTypes.TIME_STOP_LEVEL_TRACKER);
			levelTracker.remove(this);
		}
	}
	
	
	public static boolean canEntityTickInStoppedTime(Entity entity) {
		if (entity instanceof LivingEntity living) {
			LivingEntity standUser = StandUtil.getStandUser(living);
			if (standUser.isSpectator() || standUser instanceof Player player && player.isCreative()) {
				return true;
			}
			
			StandPower standPower = StandPower.get(standUser);
			if (standPower != null) {
				Optional<TimeStopEffect> entityCurTimeStop = standPower.userStandEffects.getEffectOfType(ModStandAbilities.EFFECT_TIME_STOP.get());
				if (entityCurTimeStop.isPresent()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean canEntitySeeInStoppedTime(LivingEntity entity) {
		return true;
//		StandPower standPower = StandPower.get(entity);
//		if (standPower != null) {
//			var unlockedSkills = standPower.getCurTypeData();
//			if (unlockedSkills.isSkillUnlocked("time_stop")) {
//				return true;
//			}
//		}
//		return false;
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
		nbt.putBoolean("PlayedFX", playedFX);
		NBTUtil.put(nbt, "Pos", initialPos, FUCK_MY_LIFE);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		duration = nbt.getInt("Duration");
		playedFX = nbt.getBoolean("PlayedFX");
		initialPos = NBTUtil.getOptional(nbt, "Pos", FUCK_MY_LIFE).orElseThrow();
	}
	
	
	public static void setTimeStopState(Entity entity, boolean isInTimeStop, boolean isFrozen, boolean canSee) {
		if (isInTimeStop) {
			var variables = JojoModEntityVariables.get(entity);
			variables.synchedData.set(JojoModEntityVariables.INSIDE_TIME_STOP_ZONE.param, true);
			variables.synchedData.set(JojoModEntityVariables.STOPPED_IN_TIME.param, isFrozen);
			variables.synchedData.set(JojoModEntityVariables.CAN_SEE_IN_STOPPED_TIME.param, canSee);
			variables.tickSyncDirtyData();
			if (isFrozen) {
				// should prevent the old position desync if the entity was moving at high speed
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new EntityDirectPosNoLerpPacket(entity.getId(), entity.position()));
			}
		}
		else {
			var variables = JojoModEntityVariables.getIfPresent(entity);
			if (variables != null) {
				variables.synchedData.set(JojoModEntityVariables.INSIDE_TIME_STOP_ZONE.param, false);
				variables.synchedData.set(JojoModEntityVariables.STOPPED_IN_TIME.param, false);
				variables.synchedData.set(JojoModEntityVariables.CAN_SEE_IN_STOPPED_TIME.param, true);
				variables.tickSyncDirtyData();
			}
		}
	}
	
	public static boolean getIsInsideTimeStop(Entity entity) {
		return JojoModEntityVariables.INSIDE_TIME_STOP_ZONE.get(
				() -> JojoModEntityVariables.getSynchedIfPresent(entity));
	}
	
	public static boolean getIsFrozenInTime(Entity entity) {
		return JojoModEntityVariables.STOPPED_IN_TIME.get(
				() -> JojoModEntityVariables.getSynchedIfPresent(entity));
	}
	
	public static boolean getCanSeeInTimeStopVar(Entity entity) {
		return JojoModEntityVariables.CAN_SEE_IN_STOPPED_TIME.get(
				() -> JojoModEntityVariables.getSynchedIfPresent(entity));
	}

}
