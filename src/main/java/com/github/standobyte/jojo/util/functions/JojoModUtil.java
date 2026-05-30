package com.github.standobyte.jojo.util.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.github.standobyte.jojo.config.BoolOrPlayerPref;
import com.github.standobyte.jojo.config.RotpConfig;
import com.github.standobyte.jojo.config.RotpConfig.Common;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.explosion.CustomExplosion;
import com.github.standobyte.jojo.network.s2c.BrokenBlocksParticlesAndSoundsPacket;
import com.github.standobyte.jojo.network.s2c.TrResetDeathTimePacket;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;

public class JojoModUtil {

	public static boolean canEntityDestroy(ServerLevel level, BlockPos blockPos, BlockState blockState, LivingEntity entity) {
		LivingEntity standUser = StandUtil.getStandUser(entity);
		if (RotpConfig.canStandBreakBlocks(standUser)
				&& blockState.canEntityDestroy(level, blockPos, entity)
				&& EventHooks.onEntityDestroyBlock(entity, blockPos, blockState)) {
			Player player = standUser instanceof Player pl ? pl : null;
			return player == null || level.mayInteract(player, blockPos);
		}
		return false;
	}

	@Deprecated
	public static boolean breakingBlocksEnabled(Level level) {
		Common commonConfig = JojoMod.config.getCommon();
		return commonConfig == null || commonConfig.standsBreakBlocks.get() != BoolOrPlayerPref.FALSE;
	}

	public static void blockCatchFire(Level level, BlockPos blockPos, BlockState blockState, @Nullable Direction face, @Nullable LivingEntity igniter) {
		blockState.onCaughtFire(level, blockPos, face, igniter);
		boolean blockGotRemoved = blockState.getBlock() instanceof TntBlock;
		if (blockGotRemoved) {
			CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, blockState, 
					Optional.ofNullable(level.getBlockEntity(blockPos)), Collections.emptyList());
			level.removeBlock(blockPos, false);
		}
	}

	@Deprecated
	public static boolean destroyBlock(Level level, BlockPos blockPos, boolean dropBlock, @Nullable Entity entity) {
//		BlockState oldState = dropBlock ? null /*no need to call it in this case*/ : level.getBlockState(blockPos);
//		boolean res = level.destroyBlock(blockPos, dropBlock, entity);
////		if (res && !dropBlock) {
////			CrazyDRestoreTerrainAbility.rememberBrokenBlock(level, blockPos, oldState, 
////					Optional.ofNullable(level.getBlockEntity(blockPos)), 
////					Collections.emptyList());
////		}
//		return res;
		return level.destroyBlock(blockPos, dropBlock, entity);
	}

	public static boolean dropBrokenBlock(LivingEntity entity) {
		return !(entity instanceof Player player && player.getAbilities().instabuild);
	}

	/**
	 *  Limits the amount of particles and break sounds that the blocks produce, sending it all in one packet
	 */
	public static int destroyBlocksInBulk(Collection<BlockPos> blocks, ServerLevel world, @Nullable LivingEntity entity, boolean dropItems) {
		if (world.isDebug()) {
			return -1;
		}

		Iterator<BlockPos> iter = blocks.iterator();
		while (iter.hasNext()) {
			BlockPos blockPos = iter.next();
			BlockState blockState = world.getBlockState(blockPos);
			if (world.isOutsideBuildHeight(blockPos) || blockState.isAir()
					|| !JojoModUtil.canEntityDestroy(world, blockPos, blockState, entity)) {
				iter.remove();
			}
		}
		if (blocks.isEmpty()) return 0;
		int blocksBroken = 0;

		BrokenBlocksParticlesAndSoundsPacket packet = new BrokenBlocksParticlesAndSoundsPacket();
		int minX = 30000001;
		int minY = 999;
		int minZ = 30000001;
		int maxX = -30000001;
		int maxY = -999;
		int maxZ = -30000001;

		List<Pair<ItemStack, BlockPos>> dropPositions = new ArrayList<>();

		for (BlockPos blockPos : blocks) {
			FluidState fluidState = world.getFluidState(blockPos);
			BlockState newState = fluidState.createLegacyBlock();

			BlockState oldState = world.getBlockState(blockPos);

			if (!(oldState.getBlock() instanceof BaseFireBlock)) {
				minX = Math.min(minX, blockPos.getX());
				minY = Math.min(minY, blockPos.getY());
				minZ = Math.min(minZ, blockPos.getZ());
				maxX = Math.max(maxX, blockPos.getX());
				maxY = Math.max(maxY, blockPos.getY());
				maxZ = Math.max(maxZ, blockPos.getZ());
				packet.addBlock(blockPos, oldState);
			}
			if (dropItems) {
				BlockEntity tileentity = oldState.hasBlockEntity() ? world.getBlockEntity(blockPos) : null;

				Block.getDrops(oldState, world, blockPos, tileentity, entity, ItemStack.EMPTY).forEach(itemStack -> {
					CustomExplosion.addOrAppendStack(dropPositions, itemStack, blockPos);
				});
			}
			else {
				CrazyDRestoreTerrainAbility.rememberBrokenBlock(world, blockPos, oldState, 
						Optional.ofNullable(world.getBlockEntity(blockPos)), 
						Collections.emptyList());
			}

			if (world.setBlock(blockPos, newState, 3)) {
				++blocksBroken;
			}
		}

		for (Pair<ItemStack, BlockPos> pair : dropPositions) {
			Block.popResource(world, pair.getRight(), pair.getLeft());
		}

		packet.sendToPlayers(world, minX, minY, minZ, maxX, maxY, maxZ);

		return blocksBroken;
	}

	public static void iterateOverBlocks(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Consumer<BlockPos.MutableBlockPos> action) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {
					pos.set(x, y, z);
					action.accept(pos);
				}
			}
		}
	}

	
	public static boolean canHarm(LivingEntity attacker, Entity target) {
		if (attacker == target) return false;
		Team team = attacker.getTeam();
		Team team1 = target.getTeam();
		if (team != null && team.isAlliedTo(team1) && !team.isAllowFriendlyFire()) {
			return false;
		}
		if (attacker instanceof StandEntity attackerStand) {
			return attackerStand.canAttackEntity(target);
		}
		return target instanceof LivingEntity && attacker.canAttack((LivingEntity) target);
	}

	public static void onLivingResurrect(LivingEntity entity) {
		entity.deathTime = 0;
		Level level = entity.level();
		if (!entity.level().isClientSide()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrResetDeathTimePacket(entity.getId()));
			if (entity instanceof ServerPlayer player) {
				if (!level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !player.isSpectator()) {
					player.setExperienceLevels(0);
					player.setExperiencePoints(0);
				}
			}
		}
	}


	public static Iterable<Entity> getAllEntities(Level level) {
		return level.isClientSide() ? ((ClientLevel) level).entitiesForRendering() : ((ServerLevel) level).getAllEntities();
	}

}
