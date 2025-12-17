package com.github.standobyte.jojo.util;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.jojoimpl.stands.crazydiamond.CrazyDRestoreTerrainAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

public class JojoModUtil {

	@Nullable
	public static ItemEntity dropItem(Entity entity, ItemStack item, boolean dropAround, boolean includeThrowerName) {
		if (item.isEmpty()) {
			return null;
		} else {
			double d0 = entity.getEyeY() - 0.3F;
			ItemEntity itementity = new ItemEntity(entity.level(), entity.getX(), d0, entity.getZ(), item);
			itementity.setPickUpDelay(40);
			if (includeThrowerName) {
				itementity.setThrower(entity);
			}

			RandomSource random = entity.getRandom();
			if (dropAround) {
				float f = random.nextFloat() * 0.5F;
				float f1 = random.nextFloat() * (float) (Math.PI * 2);
				itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), 0.2F, (double)(Mth.cos(f1) * f));
			} else {
				float f7 = 0.3F;
				float f8 = Mth.sin(entity.getXRot() * (float) (Math.PI / 180.0));
				float f2 = Mth.cos(entity.getXRot() * (float) (Math.PI / 180.0));
				float f3 = Mth.sin(entity.getYRot() * (float) (Math.PI / 180.0));
				float f4 = Mth.cos(entity.getYRot() * (float) (Math.PI / 180.0));
				float f5 = random.nextFloat() * (float) (Math.PI * 2);
				float f6 = 0.02F * random.nextFloat();
				itementity.setDeltaMovement(
						(double)(-f3 * f2 * f7) + Math.cos((double)f5) * (double)f6,
						(double)(-f8 * f7 + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F),
						(double)(f4 * f2 * f7) + Math.sin((double)f5) * (double)f6);
			}

			return itementity;
		}
	}

	public static boolean canEntityDestroy(ServerLevel level, BlockPos blockPos, BlockState blockState, LivingEntity entity) {
		if (breakingBlocksEnabled(level)
				&& blockState.canEntityDestroy(level, blockPos, entity)
				&& EventHooks.onEntityDestroyBlock(entity, blockPos, blockState)) {
			Player player = null;
			if (entity instanceof Player) {
				player = (Player) entity;
			}
			else if (entity instanceof StandEntity) {
				LivingEntity standUser = ((StandEntity) entity).getUser();
				if (standUser instanceof Player) {
					player = (Player) standUser;
				}
			}
			return player == null || level.mayInteract(player, blockPos);
		}
		return false;
	}

	public static boolean breakingBlocksEnabled(Level level) {
		return true;
		// FIXME jojoAbilitiesBreakBlocks gamerule
//		return level.getGameRules().getBoolean(ModGamerules.BREAK_BLOCKS);
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

}
